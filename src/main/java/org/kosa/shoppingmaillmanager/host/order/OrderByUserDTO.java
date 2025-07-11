package org.kosa.shoppingmaillmanager.host.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderByUserDTO {
  private String user_id;
  private String order_id;
  private String order_date;
  private String product_id;
  private String product_name;
  private int total_price;
  private String payment_method;
  private String status;
}
