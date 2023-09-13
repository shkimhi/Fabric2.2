/*
 * SPDX-License-Identifier: Apache-2.0
 *//*


package org.hyperledger.fabric.samples.fabcar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class HotelTest {

    @Nested
    class Equality {

        @Test
        public void isReflexive() {
            Hotel hotel = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);

            assertThat(hotel).isEqualTo(hotel);
        }

        @Test
        public void isSymmetric() {
            Hotel hotelA = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);
            Hotel hotelB = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);

            assertThat(hotelA).isEqualTo(hotelB);
            assertThat(hotelB).isEqualTo(hotelA);
        }

        @Test
        public void isTransitive() {
            Hotel hotelA = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);
            Hotel hotelB = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);
            Hotel hotelC = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);

            assertThat(hotelA).isEqualTo(hotelB);
            assertThat(hotelB).isEqualTo(hotelC);
            assertThat(hotelA).isEqualTo(hotelC);
        }

        @Test
        public void handlesInequality() {
            Hotel hotelA = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);
            Hotel hotelB = new Hotel("Ford", "Mustang", "red", "Brad", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);

            assertThat(hotelA).isNotEqualTo(hotelB);
        }

        @Test
        public void handlesOtherObjects() {
            Hotel hotelA = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);
            String carB = "not a car";

            assertThat(hotelA).isNotEqualTo(carB);
        }

        @Test
        public void handlesNull() {
            Hotel hotel = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);

            assertThat(hotel).isNotEqualTo(null);
        }
    }

    @Test
    public void toStringIdentifiesCar() {
        Hotel hotel = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);

        assertThat(hotel.toString()).isEqualTo("Hotel@db8e149a[userid=test1, rname=seok, cin=05-17, membernum=520, cout=05-21, aname=썬앤제이드]");
    }
}
*/
