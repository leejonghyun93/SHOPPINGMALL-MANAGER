package org.kosa.shoppingmaillmanager.host.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "tb_product_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPTION_ID")
    private Integer optionId;

    @Column(name = "PRODUCT_ID")
    private Integer productId;

    @Column(name = "OPTION_NAME")
    private String optionName;

    @Column(name = "SALE_PRICE")
    private Integer salePrice;

    @Column(name = "STOCK")
    private Integer stock;

    @Column(name = "STATUS")
    private String status;
}
