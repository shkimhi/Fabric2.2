/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.fabcar;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Hotel {

    @Property()
    private final String user_id;

    @Property()
    private final String guest_name;

    @Property()
    private final String check_in;

    @Property()
    private final String guest_num;

    @Property()
    private final String check_out;

    @Property()
    private final String accommodation_name;
    @Property()
    private final String accommodation_address;
    @Property()
    private final String bid_amount;
    @Property()
    private final String accommodation_info;
    @Property()
    private final String accommodation_hp;

    public String getuser_id() {
        return user_id;
    }

    public String getguest_name() {
        return guest_name;
    }

    public String getcheck_in() {
        return check_in;
    }

    public String getguest_num() {
        return guest_num;
    }
    public String getcheck_out() {
        return check_out;
    }
    public String getaccommodation_name() {
        return accommodation_name;
    }

    public String getaccommodation_address() {
        return accommodation_address;
    }

    public String getbid_amount() {
        return bid_amount;
    }

    public String getaccommodation_info() {
        return accommodation_info;
    }

    public String getaccommodation_hp() {
        return accommodation_hp;
    }

    public Hotel(@JsonProperty("user_id") final String user_id, @JsonProperty("guest_name") final String guest_name,
                 @JsonProperty("check_in") final String check_in, @JsonProperty("guest_num") final String guest_num, @JsonProperty("check_out") final String check_out,
                 @JsonProperty("accommodation_name") final String accommodation_name, @JsonProperty("accommodation_address") final String accommodation_address,
                 @JsonProperty("bid_amount") final String bid_amount, @JsonProperty("accommodation_info") final String accommodation_info, @JsonProperty("accommodation_hp") final String accommodation_hp) {
        this.user_id = user_id;
        this.guest_name = guest_name;
        this.check_in = check_in;
        this.guest_num = guest_num;
        this.check_out = check_out;
        this.accommodation_name = accommodation_name;
        this.accommodation_address = accommodation_address;
        this.bid_amount = bid_amount;
        this.accommodation_info = accommodation_info;
        this.accommodation_hp = accommodation_hp;
    }

/*
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Hotel other = (Hotel) obj;

        return Objects.deepEquals(new String[] {getuser_id(), getguest_name(), getcheck_in(), getguest_num(), getcheck_out(), getaccommodation_name()},
                new String[] {other.getuser_id(), other.getguest_name(), other.getcheck_in(), other.getguest_num(), other.getcheck_out(), other.getaccommodation_name()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getuser_id(), getguest_name(), getcheck_in(), getguest_num(), getcheck_out(), getaccommodation_name());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + "[user_id=" + user_id + ", guest_name="
                + guest_name + ", check_in=" + check_in + ", guest_num=" + guest_num + ", check_out=" + check_out + ", accommodation_name=" + accommodation_name + "]";
    }
*/

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Hotel hotel = (Hotel) o;
        return Objects.equals(user_id, hotel.user_id) && Objects.equals(guest_name, hotel.guest_name)
                && Objects.equals(check_in, hotel.check_in) && Objects.equals(guest_num, hotel.guest_num)
                && Objects.equals(check_out, hotel.check_out) && Objects.equals(accommodation_name, hotel.accommodation_name)
                && Objects.equals(accommodation_address, hotel.accommodation_address) && Objects.equals(bid_amount, hotel.bid_amount)
                && Objects.equals(accommodation_info, hotel.accommodation_info) && Objects.equals(accommodation_hp, hotel.accommodation_hp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, guest_name, check_in, guest_num, check_out, accommodation_name, accommodation_address, bid_amount, accommodation_info, accommodation_hp);
    }

    @Override
    public String toString() {
        return "Hotel{" + "user_id='" + user_id + '\'' + ", guest_name='" + guest_name + '\''
                + ", check_in='" + check_in + '\'' + ", guest_num='" + guest_num + '\''
                + ", check_out='" + check_out + '\'' + ", accommodation_name='" + accommodation_name + '\''
                + ", accommodation_address='" + accommodation_address + '\'' + ", bid_amount='" + bid_amount + '\''
                + ", accommodation_info='" + accommodation_info + '\'' + ", accommodation_hp='" + accommodation_hp + '\'' + '}';
    }
}
