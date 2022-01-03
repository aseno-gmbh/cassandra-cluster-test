package de.aseno.spikes.order;



import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.Version;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@SuppressWarnings("serial")
@Table(value = "starter_orders")
@Data
public class Order implements Serializable {

    @PrimaryKey
    private OrderPrimaryKey key;

    /**
     * The @Version annotation provides syntax similar to that of JPA in the context of Cassandra and 
     * makes sure updates are only applied to rows with a matching version. Optimistic Locking leverages 
     * Cassandra’s lightweight transactions to conditionally insert, update and delete rows. Therefore, INSERT 
     * statements are executed with the IF NOT EXISTS condition. 
     */
    @Version Long version;

    @Column("product_quantity")
    @CassandraType(type = CassandraType.Name.INT)
    private Integer productQuantity;

    @Column("product_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String productName;

    @CassandraType(type = CassandraType.Name.DECIMAL)
    @Column("product_price")
    private Float productPrice;

    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    @Column("added_to_order_at")
    private Instant addedToOrderTimestamp;

}