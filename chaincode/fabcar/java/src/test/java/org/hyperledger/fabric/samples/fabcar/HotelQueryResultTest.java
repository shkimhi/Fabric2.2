/*
 * SPDX-License-Identifier: Apache-2.0
 *//*


package org.hyperledger.fabric.samples.fabcar;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class HotelQueryResultTest {

    @Nested
    class Equality {

        @Test
        public void isReflexive() {
            HotelQueryResult cqr = new HotelQueryResult("CAR1", new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));

            assertThat(cqr).isEqualTo(cqr);
        }

        @Test
        public void isSymmetric() {
            Hotel hotel = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);
            HotelQueryResult cqrA = new HotelQueryResult("CAR1", hotel);
            HotelQueryResult cqrB = new HotelQueryResult("CAR1", hotel);

            assertThat(cqrA).isEqualTo(cqrB);
            assertThat(cqrB).isEqualTo(cqrA);
        }

        @Test
        public void isTransitive() {
            Hotel hotel = new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp);
            HotelQueryResult cqrA = new HotelQueryResult("CAR1", hotel);
            HotelQueryResult cqrB = new HotelQueryResult("CAR1", hotel);
            HotelQueryResult cqrC = new HotelQueryResult("CAR1", hotel);

            assertThat(cqrA).isEqualTo(cqrB);
            assertThat(cqrB).isEqualTo(cqrC);
            assertThat(cqrA).isEqualTo(cqrC);
        }

        @Test
        public void handlesKeyInequality() {
            HotelQueryResult cqrA = new HotelQueryResult("CAR1", new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));
            HotelQueryResult cqrB = new HotelQueryResult("CAR2", new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));

            assertThat(cqrA).isNotEqualTo(cqrB);
        }

        @Test
        public void handlesRecordInequality() {
            HotelQueryResult cqrA = new HotelQueryResult("CAR1", new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));
            HotelQueryResult cqrB = new HotelQueryResult("CAR1", new Hotel("Ford", "Mustang", "red", "Brad", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));

            assertThat(cqrA).isNotEqualTo(cqrB);
        }

        @Test
        public void handlesKeyRecordInequality() {
            HotelQueryResult cqrA = new HotelQueryResult("CAR1", new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));
            HotelQueryResult cqrB = new HotelQueryResult("CAR2", new Hotel("Ford", "Mustang", "red", "Brad", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));

            assertThat(cqrA).isNotEqualTo(cqrB);
        }

        @Test
        public void handlesOtherObjects() {
            HotelQueryResult cqrA = new HotelQueryResult("CAR1", new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));
            String cqrB = "not a car";

            assertThat(cqrA).isNotEqualTo(cqrB);
        }

        @Test
        public void handlesNull() {
            HotelQueryResult cqr = new HotelQueryResult("CAR1", new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));

            assertThat(cqr).isNotEqualTo(null);
        }
    }

    @Test
    public void toStringIdentifiesCarQueryResult() {
        HotelQueryResult cqr = new HotelQueryResult("CAR1", new Hotel("test1", "seok", "05-17", "520", "05-21", "썬앤제이드", aaddress, vidding, ainfo, ahp));

        assertThat(cqr.toString()).isEqualTo("HotelQueryResult@df5d04fe [key=CAR1, "
                + "record=Hotel@db8e149a[userid=test1, rname=seok, cin=05-17, membernum=520, cout=05-21, aname=썬앤제이드]]");
    }
}
*/
