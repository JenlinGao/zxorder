package com.zxin.order.repository;

import com.zxin.order.dataobject.OrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;


// 里面方法不需要写，直接调用已有方法即可
public interface OrderMasterRepository extends JpaRepository<OrderMaster, String> {
}
