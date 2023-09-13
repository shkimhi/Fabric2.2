/*
 * SPDX-License-Identifier: Apache-2.0
 *//*


package org.hyperledger.fabric.samples.fabcar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

public final class FabHotelTest {

    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    private final class MockCarResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> carList;

        MockCarResultsIterator() {
            super();

            carList = new ArrayList<KeyValue>();

            carList.add(new MockKeyValue("1",
                    "{\"cin\":\"05-17\",\"userid\":\"test1\",\"rname\":\"seok\",\"membernum\":\"520\",\"cout\":\"05-21\",\"aname\":\"썬앤제이드\"}"));
            carList.add(new MockKeyValue("2",
                    "{\"cin\":\"red\",\"userid\":\"Ford\",\"rname\":\"Mustang\",\"membernum\":\"Brad\",\"cout\":\"05-21\",\"aname\":\"썬앤제이드\"}"));
            carList.add(new MockKeyValue("3",
                    "{\"cin\":\"green\",\"userid\":\"Hyundai\",\"rname\":\"Tucson\",\"membernum\":\"Jin Soo\",\"cout\":\"05-21\",\"aname\":\"썬앤제이드\"}"));
            carList.add(new MockKeyValue("8",
                    "{\"cin\":\"violet\",\"userid\":\"Fiat\",\"rname\":\"Punto\",\"membernum\":\"Pari\",\"cout\":\"05-21\",\"aname\":\"썬앤제이드\"}"));
            carList.add(new MockKeyValue("10",
                    "{\"cin\":\"brown\",\"userid\":\"Holden\",\"rname\":\"Barina\",\"membernum\":\"Shotaro\",\"cout\":\"05-21\",\"aname\":\"썬앤제이드\"}"));
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return carList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

    }

    @Test
    public void invokeUnknownTransaction() {
        FabHotel contract = new FabHotel();
        Context ctx = mock(Context.class);

        Throwable thrown = catchThrowable(() -> {
            contract.unknownTransaction(ctx);
        });

        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                .hasMessage("Undefined contract method called");
        assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo(null);

        verifyZeroInteractions(ctx);
    }

    @Nested
    class InvokeQueryHotelTransaction {

        @Test
        public void whenCarExists() {
            FabHotel contract = new FabHotel();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("1"))
                    .thenReturn("{\"cin\":\"05-17\",\"userid\":\"test1\",\"rname\":\"seok\",\"membernum\":\"520\",\"cout\":\"05-21\",\"aname\":\"썬앤제이드\"}");

            Hotel hotel = contract.queryHotel(ctx, "1");

            assertThat(hotel).isEqualTo(new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));
        }

        @Test
        public void whenCarDoesNotExist() {
            FabHotel contract = new FabHotel();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.queryHotel(ctx, "1");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("키 1 에 해당하는 예약 정보가 없습니다.");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("HOTEL_NOT_FOUND".getBytes());
        }
    }

    @Test
    void invokeInitLedgerTransaction() {
        FabHotel contract = new FabHotel();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);

        contract.initLedger(ctx);

        InOrder inOrder = inOrder(stub);
        inOrder.verify(stub).putStringState("1",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-17\",\"cout\":\"05-21\",\"rname\":\"김석현\",\"membernum\":\"520\",\"userid\":\"test1\"}");
        inOrder.verify(stub).putStringState("2",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-17\",\"cout\":\"05-21\",\"rname\":\"김지현\",\"membernum\":\"520\",\"userid\":\"ID123456\"}");
        inOrder.verify(stub).putStringState("3",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-17\",\"cout\":\"05-21\",\"rname\":\"박민우\",\"membernum\":\"520\",\"userid\":\"ID114561\"}");
        inOrder.verify(stub).putStringState("4",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-17\",\"cout\":\"05-21\",\"rname\":\"이수연\",\"membernum\":\"520\",\"userid\":\"ID789012\"}");
        inOrder.verify(stub).putStringState("5",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-19\",\"cout\":\"05-22\",\"rname\":\"최승호\",\"membernum\":\"520\",\"userid\":\"ID901234\"}");
        inOrder.verify(stub).putStringState("6",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-19\",\"cout\":\"05-22\",\"rname\":\"정은지\",\"membernum\":\"520\",\"userid\":\"ID567890\"}");
        inOrder.verify(stub).putStringState("7",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-19\",\"cout\":\"05-22\",\"rname\":\"강태호\",\"membernum\":\"520\",\"userid\":\"ID890123\"}");
        inOrder.verify(stub).putStringState("8",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-19\",\"cout\":\"05-22\",\"rname\":\"윤지우\",\"membernum\":\"520\",\"userid\":\"ID234567\"}");
        inOrder.verify(stub).putStringState("9",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-19\",\"cout\":\"05-22\",\"rname\":\"한민서\",\"membernum\":\"520\",\"userid\":\"ID157482\"}");
        inOrder.verify(stub).putStringState("10",
                "{\"aname\":\"썬앤제이드\",\"cin\":\"05-19\",\"cout\":\"05-22\",\"rname\":\"신나연\",\"membernum\":\"520\",\"userid\":\"ID123481\"}");
    }

    @Nested
    class InvokeCreateHotelTransaction {

        @Test
        public void whenCarExists() {
            FabHotel contract = new FabHotel();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("1"))
                    .thenReturn("{\"userid\":\"test1\",\"rname\":\"seok\",\"cin\":\"05-17\",\"membernum\":\"520\",\"cout\":\"05-21\",\"aname\":\"썬앤제이드\"}");

            Throwable thrown = catchThrowable(() -> {
                contract.createHotel(ctx, "1", "Nissan", "Leaf", "green", "Siobhán", "05-21", "썬앤제이드");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("예약정보가 이미 존재합니다.");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("HOTEL_ALREADY_EXISTS".getBytes());
        }

        @Test
        public void whenCarDoesNotExist() {
            FabHotel contract = new FabHotel();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("1")).thenReturn("");

            Hotel hotel = contract.createHotel(ctx, "1", "Nissan", "Leaf", "green", "Siobhán", "05-21", "썬앤제이드");

            assertThat(hotel).isEqualTo(new Hotel("Nissan", "Leaf", "green", "Siobhán", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));
        }
    }

    @Test
    void invokeQueryAllCarsTransaction() {
        FabHotel contract = new FabHotel();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStateByRange("", "")).thenReturn(new MockCarResultsIterator());

        String cars = contract.queryAllHotels(ctx);

        assertThat(cars).isEqualTo("[{\"key\":\"1\","
                + "\"record\":{\"aname\":\"썬앤제이드\",\"cin\":\"05-17\",\"cout\":\"05-21\",\"rname\":\"seok\",\"membernum\":\"520\",\"userid\":\"test1\"}},"
                + "{\"key\":\"2\","
                + "\"record\":{\"aname\":\"썬앤제이드\",\"cin\":\"red\",\"cout\":\"05-21\",\"rname\":\"Mustang\",\"membernum\":\"Brad\",\"userid\":\"Ford\"}},"
                + "{\"key\":\"3\","
                + "\"record\":{\"aname\":\"썬앤제이드\",\"cin\":\"green\",\"cout\":\"05-21\",\"rname\":\"Tucson\",\"membernum\":\"Jin Soo\",\"userid\":\"Hyundai\"}},"
                + "{\"key\":\"8\","
                + "\"record\":{\"aname\":\"썬앤제이드\",\"cin\":\"violet\",\"cout\":\"05-21\",\"rname\":\"Punto\",\"membernum\":\"Pari\",\"userid\":\"Fiat\"}},"
                + "{\"key\":\"10\","
                + "\"record\":{\"aname\":\"썬앤제이드\",\"cin\":\"brown\",\"cout\":\"05-21\",\"rname\":\"Barina\",\"membernum\":\"Shotaro\",\"userid\":\"Holden\"}}]");
    }

    @Nested
    class ChangeCarmembernumTransaction {

*/
/*
        @Test
        public void whenCarExists() {
            FabHotel contract = new FabHotel();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("1"))
                    .thenReturn("{\"cin\":\"05-17\",\"userid\":\"test1\",\"rname\":\"seok\",\"membernum\":\"520\",\"cout\":\"05-21\",\"aname\":\"썬앤제이드\"}");

            Hotel hotel = contract.changeHotelmembernum(ctx, "1", "Dr Evil");

            assertThat(hotel).isEqualTo(new Hotel("test1", "seok", "05-17", "Dr Evil", "05-21", "썬앤제이드"));
        }
*//*


*/
/*
        @Test
        public void whenCarDoesNotExist() {
            FabHotel contract = new FabHotel();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.changeHotelmembernum(ctx, "1", "Dr Evil");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Car 1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("HOTEL_NOT_FOUND".getBytes());
        }
*//*

    }
*/
/*
    @Test
    public void testDeleteHotel() {
        FabHotel contract = new FabHotel();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        String existingHotelState = "{\"key\":\"1\",\"name\":\"Hotel A\",\"location\":\"City X\"}";
        when(stub.getStringState("1")).thenReturn(existingHotelState);

        // Call the deleteHotel function
        contract.deleteHotel(ctx, "1");

        // Verify that the deleteState method was called with the correct key
        berify(stub).delState("1");
    }
*//*


*/
/*
    @Test
    public void whenuserIdExists() {
        FabCar contract = new FabCar();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        String userId = "test1";
        when(stub.getStringState(userId))
                .thenReturn("{\"cin\":\"05-17\",\"userid\":\"test1\",\"rname\":\"seok\",\"membernum\":\"520\",\"cout\":\"05-21\",\"aname\":\"썬앤제이드\"}");

        Car car = contract.queryUser(ctx, "test1");

        assertThat(car).isEqualTo(new Car("test1", "seok", "05-17", "520", "05-21", "썬앤제이드"));
    }
*//*


}
*/
