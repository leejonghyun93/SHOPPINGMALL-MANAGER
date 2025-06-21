package org.kosa.shoppingmaillmanager.host.order;

import lombok.Data;

@Data
public class OrderItemDTO {
    private int quantity;
    private String item_name;
    private String item_image_url;
    private String item_status;
    private int item_total_price;
    private int item_delivery_fee;
    private int item_point_earned;
}
