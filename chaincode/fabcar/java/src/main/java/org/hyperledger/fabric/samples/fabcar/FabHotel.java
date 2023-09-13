/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.fabcar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

/**
 * Java implementation of the Fabric Car Contract described in the Writing Your
 * First Application tutorial
 */
@Contract(
        name = "FabCar",
        info = @Info(
                title = "FabCar contract",
                description = "The hyperlegendary car contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.carr@example.com",
                        name = "F Carr",
                        url = "https://hyperledger.example.com")))
@Default
public final class FabHotel implements ContractInterface {

    private final Genson genson = new Genson();

    private enum FabHotelErrors {
        HOTEL_NOT_FOUND,
        HOTEL_ALREADY_EXISTS
    }

    /**
     * Retrieves a car with the specified key from the ledger.
     *
     * @param ctx the transaction context
     * @param key the key
     * @return the Car found on the ledger if there was one
     */
    @Transaction()
    public String queryHotel(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        String hotelState = stub.getStringState(key);

        if (hotelState.isEmpty()) {
            String errorMessage = String.format("키 %s 에 해당하는 예약 정보가 없습니다.", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabHotelErrors.HOTEL_NOT_FOUND.toString());
        }

        Hotel hotel = genson.deserialize(hotelState, Hotel.class);

        return hotelState;
    }
    @Transaction()
    public void deleteHotel(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        String hotelState = stub.getStringState(key);
        // 존재하는지 확인
        if (!hotelState.isEmpty()) {
            // 삭제
            stub.delState(key);
            System.out.println("Hotel record deleted for key: " + key);
        } else {
            String errorMessage = String.format("키 %s 에 해당하는 예약 정보가 없습니다", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabHotelErrors.HOTEL_NOT_FOUND.toString());
        }
    }

    @Transaction()
    public String queryUser(final Context ctx, final String user_id) {
        ChaincodeStub stub = ctx.getStub();

        List<HotelQueryResult> queryResults = new ArrayList<>(); // 호텔 쿼리 결과를 저정할 리스성트 생
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", ""); // 값 전체검색

        for (KeyValue result : results) { // (데이터형 접근변수명 : 배열)
            String hotelState = result.getStringValue();

            if (hotelState.isEmpty()) {
                String errorMessage = String.format("%s로 예약된 예약정보가 없습니다.", user_id);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, FabHotelErrors.HOTEL_NOT_FOUND.toString());
            }
            Hotel hotel = genson.deserialize(hotelState, Hotel.class);
            queryResults.add(new HotelQueryResult(result.getKey(), hotel)); //queryResults List에 추가
        }

        List<HotelQueryResult> matchingResults = queryResults.stream()
                //각 요소에 대해 평가식을 진행 후 user_id와 일치하는 결과만 필터함.
                .filter(hotelQueryResult -> hotelQueryResult.getRecord().getuser_id().equals(user_id))
                .collect(Collectors.toList()); //스트림에서 작업한 결과를 담은 List로 반환
        System.out.println(queryResults);

        if (matchingResults.isEmpty()) {
            String errorMessage = String.format("%s로 예약된 예약정보가 없습니다.", user_id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabHotelErrors.HOTEL_NOT_FOUND.toString());
        }

        final String response = genson.serialize(matchingResults); // String으로 직렬화 후 반환
        return response;
    }


    /**
     * Creates some initial Cars on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction()
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        String[] hotelData = {
        };

        for (int i = 1; i <= hotelData.length; i++) {
            String key = String.format("%d", i);

            Hotel hotel = genson.deserialize(hotelData[i - 1], Hotel.class);
            String hotelState = genson.serialize(hotel);
            stub.putStringState(key, hotelState);
        }
    }

    /**
     * Creates a new car on the ledger.
     *
     * @param ctx    the transaction context
     * @param key    the key for the new car
     * @param user_id the user_id of the new car
     * @param guest_name  the guest_name of the new car
     * @param check_in    the check_in of the new car
     * @param guest_num   the guest_num of the new car
     * @return the created Car
     */
    @Transaction()
    public Hotel createHotel(final Context ctx, final String key, final String user_id, final String guest_name,
                           final String check_in, final String guest_num, final String check_out, final String accommodation_name, final String accommodation_address,
                        final String bid_amount, final String accommodation_info, final String accommodation_hp) {
        ChaincodeStub stub = ctx.getStub();

        String hotelState = stub.getStringState(key);
        if (!hotelState.isEmpty()) {
            String errorMessage = String.format("예약정보가 이미 존재합니다."+ key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabHotelErrors.HOTEL_ALREADY_EXISTS.toString());
        }

        Hotel hotel = new Hotel(user_id, guest_name, check_in, guest_num, check_out, accommodation_name, accommodation_address, bid_amount, accommodation_info, accommodation_hp);
        hotelState = genson.serialize(hotel);
        stub.putStringState(key, hotelState); //원장에 직렬화된 hotelState를 key와 함께 저장

        return hotel;
    }

    /**
     * Retrieves all cars from the ledger.
     *
     * @param ctx the transaction context
     * @return array of Cars found on the ledger
     */
    @Transaction()
    public String queryAllHotels(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

/*
        final String startKey = "1";
        final String endKey = "99";
*/
        List<HotelQueryResult> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result : results) {
            Hotel hotel = genson.deserialize(result.getStringValue(), Hotel.class);
            queryResults.add(new HotelQueryResult(result.getKey(), hotel));
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    /**
     * Changes the guest_num of a car on the ledger.
     *
     * @param ctx     the transaction context
     * @param key     the key
     * @param newguest_num the new guest_num
     * @return the updated Car
     */
/*
    @Transaction()
    public Hotel changeHotelguest_num(final Context ctx, final String key, final String newguest_num) {
        ChaincodeStub stub = ctx.getStub();

        String hotelState = stub.getStringState(key);

        if (hotelState.isEmpty()) {
            String errorMessage = String.format("Car %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, FabHotelErrors.HOTEL_NOT_FOUND.toString());
        }

        Hotel hotel = genson.deserialize(hotelState, Hotel.class);

        Hotel newHotel = new Hotel(hotel.getuser_id(), hotel.getguest_name(), hotel.getcheck_in(), newguest_num, hotel.getcheck_out(), hotel.getaccommodation_name());
        String newHotelState = genson.serialize(newHotel);
        stub.putStringState(key, newHotelState);

        return newHotel;
    }
*/
}
