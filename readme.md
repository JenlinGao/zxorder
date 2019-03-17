# 项目笔记


* [一、商品服务编码(product)](#一商品服务编码product)

  * [1、基础环境搭建](#1基础环境搭建)
  * [2、查询商品](2查询商品)
  * [3、构造数据](#3构造数据)
  * [4、商品部分测试和总结](#4商品部分测试和总结)
* [二、订单服务编码(order)](#二订单服务编码order)

  * [1、基础环境搭建](#1基础环境搭建)
  * [2、查询订单](#2查询订单)
  * [3、数据传输对象DTO](#3数据传输对象dto)
  * [4、数据校验和Controller编写](#4数据校验和controller编写)
  * [5、订单部分测试和总结](#5订单部分测试和总结)
* [三、应用通信(远程调用)](#三应用通信远程调用)

  * [1、完成从order服务到product服务查询商品的远程调用](#1完成从order服务到product服务查询商品的远程调用)
  * [2、扣库存(远程调用)](#2扣库存远程调用)
  * [3、整合接口，打通下单流程](#3整合接口打通下单流程)

## 一、商品服务编码(product)

总的业务逻辑:

* 1、查询所有在架的商品
* 2、获取类目type列表
* 3、查询类目
* 4、构造数据

### 1、基础环境搭建

在`product`模块中导入如下依赖，即spring-data和mysql。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

基本的配置文件`application.yml`:

```yaml
spring:
  application:
    name: product
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    username: root
    password: root
    url: jdbc:mysql://127.0.0.1:3306/zxorder?characterEncoding=utf-8&useSSL=false
  jpa:
    show-sql: true  # jpa方便调试
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

```

两个实体类:

```java
@Data
@Entity
public class ProductCategory {

    @Id
    @GeneratedValue  // 自增
    private Integer categoryId;

    /** 类目名字. */
    private String categoryName;

    /** 类目编号. */
    private Integer categoryType;

    private Date createTime;

    private Date updateTime;
}

```

```java
@Data
//@Table(name = "T_proxxx")
@Entity // 标明和数据库表对应的实体
public class ProductInfo {

    @Id
    private String productId;

    /** 名字. */
    private String productName;

    /** 单价. */
    private BigDecimal productPrice;

    /** 库存. */
    private Integer productStock;

    /** 描述. */
    private String productDescription;

    /** 小图. */
    private String productIcon;

    /** 状态, 0正常1下架. */
    private Integer productStatus;

    /** 类目编号. */
    private Integer categoryType;

    private Date createTime;

    private Date updateTime;
}

```

### 2、查询商品

然后是查询类目和查询商品信息的`Repository`: (使用SpringData)

```java
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    List<ProductCategory> findByCategoryTypeIn(List<Integer> categoryTypeList);
}

```

```java
// 第一个参数是实体，第二个参数是主键的类型
public interface ProductInfoRepository extends JpaRepository<ProductInfo, String> {

    List<ProductInfo> findByProductStatus(Integer productStatus);
}

```

然后是查询在架商品的`Service`:

```java
public interface ProductService {

    /**
     * 查询所有在架 (up)商品
     */
    List<ProductInfo> findUpAll();
}
```

```java
@Service
public class ProductServiceImpl implements ProductService{

    // Dao层注入到Service层
    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Override
    public List<ProductInfo> findUpAll() {
        return productInfoRepository.findByProductStatus(ProductStatusEnum.UP.getCode()); // 枚举, 在架的状态
    }
}

```

`ProductServiceImpl`中使用到的`Enum`:

```java
/**
 * 商品上下架状态
 */
@Getter
public enum ProductStatusEnum {

    UP(0, "在架"),
    DOWN(1, "下架"),
    ;

    private Integer code;
    private String msg;

    ProductStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
```

### 3、构造数据

下面是返回到前端的三个`ViewObject`:

```java
/**
 * http请求返回的最外层对象 , 第一层
 * @param <T>
 */
@Data
public class ResultVO<T> {
    /**
     * 错误码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String msg;

    private T data;
}

```

```java
// http请求返回的第二层对象 (就是类目)
@Data
public class ProductVO {

    @JsonProperty("name")  // 因为返回给前段又必须是 name, 所以加上这个注解
    private String categoryName;

    @JsonProperty("type")
    private Integer categoryType;

    @JsonProperty("foods")
    List<ProductInfoVO> productInfoVOList;
}
```

```java
// 第三层
@Data
public class ProductInfoVO {

    @JsonProperty("id")
    private String productId;

    @JsonProperty("name")
    private String productName;

    @JsonProperty("price")
    private BigDecimal productPrice;

    @JsonProperty("description")
    private String productDescription;

    @JsonProperty("icon")
    private String productIcon;
}

```

最后的查询分类的 Controller :

```java
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    /**
     * 1、查询所有在架的商品
     * 2、获取类目type列表
     * 3、查询类目
     * 4、构造数据
     */
    @GetMapping("/list")
    public ResultVO<ProductVO> list(){
        // 1、查询所有在架的商品
        List<ProductInfo> productInfoList = productService.findUpAll();

        // 2、获取类目type列表 (所有商品的类目列表)
        List<Integer> categoryTypeList = productInfoList.stream()
                .map(ProductInfo::getCategoryType)
                .collect(Collectors.toList());

        // 3、从数据库查询类目 -- > 查询到所有类目
        List<ProductCategory> categoryList = categoryService.findByCategoryTypeIn(categoryTypeList);

        // 4、构造数据
        List<ProductVO> productVOList = new ArrayList<>(); //第二层的商品
        for(ProductCategory productCategory : categoryList){ // 遍历类目
            ProductVO productVo = new ProductVO();//外层的一个
            productVo.setCategoryName(productCategory.getCategoryName());
            productVo.setCategoryType(productCategory.getCategoryType());

            // 里面又是一个list -> 商品list
            List<ProductInfoVO> productInfoVoList = new ArrayList<>();
            for(ProductInfo productInfo : productInfoList){
                if(productInfo.getCategoryType().equals(productCategory.getCategoryType())) {

                    ProductInfoVO productInfoVO = new ProductInfoVO();
                    // source, target, 将source的属性拷贝的target中，省略了5个setter方法
                    BeanUtils.copyProperties(productInfo, productInfoVO);
                    productInfoVoList.add(productInfoVO);
                }
            }

            productVo.setProductInfoVOList(productInfoVoList);
            productVOList.add(productVo);
        }


        // 最外层的 --> 返回
        ResultVO resultVO = new ResultVO();
        resultVO.setData(productVOList);
        resultVO.setCode(0);
        resultVO.setMsg("成功");
        return resultVO;
    }
}

```

### 4、商品部分测试和总结

运行结果:

![1_1.png](images/1_1.png)

将上面的返回的`ViewObject`封装成工具类:

```java
/**
 * 返回 ViesObject的工具类
 */
public class ResultVOUtil {

    // 将返回做成了工具类
    public static ResultVO success(Object object){
        ResultVO resultVO = new ResultVO();
        resultVO.setData(object);
        resultVO.setCode(0);
        resultVO.setMsg("成功");
        return resultVO;
    }
}

```

然后代码改成:

```java
@GetMapping("/list")
public ResultVO<ProductVO> list(){
   
    //.... 省略中间

   // 最外层的 --> 返回
   return ResultVOUtil.success(productVOList); //包装成返回的工具类
}
```

商品服务代码就写到这里，上一张整体代码框架图:



<div align="center'> <img src="images/1_2.png"></div><br>



## 二、订单服务编码(order)

业务逻辑(这里用到了微服务)

* 1、参数校验；
* 2、查询商品信息 (需要调用远程服务 `Feign`；
* 3、计算总价；
* 4、扣库存(调用商品服务)；
* 5、订单入库；

注意两个表`order_detail`和`order_master`的关系。

`order_master`和`order_detail`是一对多的关系。



### 1、基础环境搭建

编码

基本`application.yml`配置:

也是先写实体类:

```java
@Data
@Entity
public class OrderDetail {

    @Id
    private String detailId;

    /** 订单id. */
    private String orderId;

    /** 商品id. */
    private String productId;

    /** 商品名称. */
    private String productName;

    /** 商品单价. */
    private BigDecimal productPrice;

    /** 商品数量. */
    private Integer productQuantity;

    /** 商品小图. */
    private String productIcon;
}
```

```java
@Data
@Entity
public class OrderMaster {

    /** 订单id. */
    @Id
    private String orderId;

    /** 买家名字. */
    private String buyerName;

    /** 买家手机号. */
    private String buyerPhone;

    /** 买家地址. */
    private String buyerAddress;

    /** 买家微信Openid. */
    private String buyerOpenid;

    /** 订单总金额. */
    private BigDecimal orderAmount;

    /** 订单状态, 默认为0新下单. */
    private Integer orderStatus;

    /** 支付状态, 默认为0未支付. */
    private Integer payStatus;

    /** 创建时间. */
    private Date createTime;

    /** 更新时间. */
    private Date updateTime;
}
```

### 2、查询订单

然后是 `dao`层:

```java
public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {

	List<OrderDetail> findByOrderId(String orderId);
}
```

```java
// 里面方法不需要写，直接调用已有方法即可
public interface OrderMasterRepository extends JpaRepository<OrderMaster, String> {
}
```

测试`OrderMasterRepository`:

![2_1.png](images/2_1.png)

测试二:

![2_2.png](images/2_2.png)

这里用到的几个枚举类:

```java
/**
 * 订单状态
 */
@Getter
public enum OrderStatusEnum {
    NEW(0, "新订单"),
    FINISHED(1, "完结"),
    CANCEL(2, "取消"),
    ;
    private Integer code;

    private String message;

    OrderStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

```java
/**
 * 支付状态
 */
@Getter
public enum PayStatusEnum {
    WAIT(0, "等待支付"),
    SUCCESS(1, "支付成功"),
    ;
    private Integer code;

    private String message;

    PayStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

```java
/**
 * 返回结果的状态 
 */
@Getter
public enum ResultEnum {
    PARAM_ERROR(1, "参数错误"),
    CART_EMPTY(2, "购物车为空"),
    ORDER_NOT_EXIST(3, "订单不存在"),
    ORDER_STATUS_ERROR(4, "订单状态错误"),
    ORDER_DETAIL_NOT_EXIST(5, "订单详情不存在"),

    ;

    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

### 3、数据传输对象DTO

然后就是写Service,但是这里由于需要`OrderMaster`和`OrderDetail`，但是又不匹配，所以我们先写一个`DTO`。

注意到`OrderDTO`最后一行有一个`OrderDetail`的`List`，相当于`OrderMaster`和`OrderDetail`一对多，然后`OrderMaster`里面就有多个`OrderDetail`。

```java
@Data
public class OrderDTO {

    /** 订单id. */
    private String orderId;

    /** 买家名字. */
    private String buyerName;

    /** 买家手机号. */
    private String buyerPhone;

    /** 买家地址. */
    private String buyerAddress;

    /** 买家微信Openid. */
    private String buyerOpenid;

    /** 订单总金额. */
    private BigDecimal orderAmount;

    /** 订单状态, 默认为0新下单. */
    private Integer orderStatus;

    /** 支付状态, 默认为0未支付. */
    private Integer payStatus;

    // 这个就是转换的, OrderMaster中有多个OrderDetail
    private List<OrderDetail> orderDetailList;
}
```

> 数据传输对象（DTO)(Data Transfer Object)，是一种设计模式之间传输数据的软件应用系统。数据传输目标往往是[数据访问对象](https://baike.baidu.com/item/%E6%95%B0%E6%8D%AE%E8%AE%BF%E9%97%AE%E5%AF%B9%E8%B1%A1/3351868)从数据库中检索数据。数据传输对象与数据交互对象或数据访问对象之间的差异是一个以不具有任何行为除了存储和检索的数据（访问和存取器）。

然后就是`Service`和`ServiceImpl`:

```javascript
public interface OrderService {

    // 这个就是创建订单
    OrderDTO create(OrderDTO orderDTO);

}
```

注意这个Service还不完善:

```java
@Service
public class OrderServiceImpl implements OrderService {

    // 这里需要注入两个 Repository

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private OrderMasterRepository orderMasterRepository;

    @Override
    public OrderDTO create(OrderDTO orderDTO) {

        /**
         *
         TODO 2、查询商品信息 (需要调用远程服务 Feign)；
         TODO 3、计算总价；
         TODO 4、扣库存(调用商品服务)；
         5、订单入库；  (这个可以做)
         */

        // 5、订单入库 (OrderMaster)
        OrderMaster orderMaster = new OrderMaster();
        orderDTO.setOrderId(KeyUtil.generateUniqueKey()); // 生成唯一的订单逐渐(订单号)
        BeanUtils.copyProperties(orderDTO, orderMaster); // 将orderDTO拷贝到orderMaster
        orderMaster.setOrderAmount(new BigDecimal(5));
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());

        orderMasterRepository.save(orderMaster);
        return orderDTO;
    }
}
```

这里用到了一个生成`key`(主键的工具):

```java
public class KeyUtil {

    /**
     * 生成唯一的主键
     * 格式: 时间戳 + 随机数
     */
    public static synchronized String generateUniqueKey(){
        // 这里只是模拟唯一，不能降低到 100%的唯一
        Random rnd = new Random();
        Integer num = rnd.nextInt(900000) + 100000;
        return System.currentTimeMillis() + String.valueOf(num); // 生成一个随机的串
    }
}
```

### 4、数据校验和Controller编写

然后就是`Controller`的编写:

因为我们需要进行参数校验，所以写了一个从前端传过来数据的类(在`form`包下)：

这里进行了数据校验:

```java
/**
 * 前端API传递过来的参数
 */
@Data
public class OrderForm {

    // 传递过来的参数, 注意这里有表单验证
    /**
     * 买家姓名
     */
    @NotEmpty(message = "姓名必填")
    private String name;

    /**
     * 买家手机号
     */
    @NotEmpty(message = "手机号必填")
    private String phone;

    /**
     * 买家地址
     */
    @NotEmpty(message = "地址必填")
    private String address;

    /**
     * 买家微信openid
     */
    @NotEmpty(message = "openid必填")
    private String openid;

    /**
     * 购物车, 这里本来传递给我的是一个String ，但是也是一个字符串 ，先拿到一个字符串，然后再做处理
     */
    @NotEmpty(message = "购物车不能为空")
    private String items;
}
```

如果校验有误，我们抛出一个`OrderException`:

```java
/**
 * 订单异常
 */
public class OrderException extends RuntimeException{

    private Integer code; //异常类型

    public OrderException(Integer code, String message){
        super(message);
        this.code = code;
    }

    public OrderException(ResultEnum resultEnum){
        super(resultEnum.getMessage());
        this.code = resultEnum.getCode();
    }
}
```

>

在这个过程中，我们要将`OrderForm`转换成`OrderDTO`，我们为此写了一个`Converter`:

`orderForm -> orderDTO`  将ordrForm转换成orderDTO，这样就可以调用那个service了

```java
@Slf4j
public class OrderForm2OrderDTOConverter {

    public static OrderDTO convert(OrderForm orderForm){
        Gson gson = new Gson();
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setBuyerName(orderForm.getName());
        orderDTO.setBuyerPhone(orderForm.getPhone());
        orderDTO.setBuyerAddress(orderForm.getAddress());
        orderDTO.setBuyerOpenid(orderForm.getOpenid());

        List<OrderDetail> orderDetailList = new ArrayList<>(); //保存的
        try {
            orderDetailList = gson.fromJson(orderForm.getItems(),
                    new TypeToken<List<OrderDetail>>() {
                    }.getType());
        } catch (Exception e) {
            log.error("【json转换】错误, string={}", orderForm.getItems());
            throw new OrderException(ResultEnum.PARAM_ERROR);  //参数错误
        }

        orderDTO.setOrderDetailList(orderDetailList);
        return orderDTO;
    }
}

```

然后我们的Controller就可以调用`Service`了，Controller代码如下:

```java

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 下单步骤
       1、参数校验；
       2、查询商品信息 (需要调用远程服务 Feign；
       3、计算总价；
       4、扣库存(调用商品服务)；
       5、订单入库；
     */

    // 这里参数很多，所以构造一个对象
    // 校验，这里校验不清楚的可以看这篇博客: https://www.cnblogs.com/cjsblog/p/8946768.html
    @PostMapping("/create")
    public ResultVO<Map<String, String>> create(@Valid OrderForm orderForm,
                                                BindingResult bindingResult){

        // 参数绑定有误
        if(bindingResult.hasErrors()) {
            log.error("【创建订单】参数不正确, orderForm={}", orderForm);
            throw new OrderException(ResultEnum.PARAM_ERROR.getCode(),
                    bindingResult.getFieldError().getDefaultMessage()); // 获取到 @NotEmpty(message = "姓名必填")的错误信息
        }

        // orderForm -> orderDTO  将ordrForm转换成orderDTO，这样就可以调用那个service了
        // 调用转换器
        OrderDTO orderDTO = OrderForm2OrderDTOConverter.convert(orderForm);

        //还需要再判断一次是不是为空
        if(CollectionUtils.isEmpty(orderDTO.getOrderDetailList())){
            log.error("【创建订单】购物车为空!");
            throw new OrderException(ResultEnum.CART_EMPTY);
        }

        // 创建订单
        OrderDTO result = orderService.create(orderDTO);

        // 返回的结果
        HashMap<String, String> map = new HashMap<>();
        map.put("orderId", result.getOrderId());

        return ResultVOUtil.success(map);
    }
}

```

这个过程也用到了和`product`微服务一样的两个返回结果处理类:

```java
// 返回给前端的结果
@Data
public class ResultVO<T> {

    private Integer code;

    private String msg;

    private T data;
}
```

```java
public class ResultVOUtil {

    public static ResultVO success(Object object) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(0);
        resultVO.setMsg("成功");
        resultVO.setData(object);
        return resultVO;
    }
}
```

以及一个枚举类:

```java
/**
 * 返回结果的状态
 */
@Getter
public enum ResultEnum {
    PARAM_ERROR(1, "参数错误"),
    CART_EMPTY(2, "购物车为空"),
    ORDER_NOT_EXIST(3, "订单不存在"),
    ORDER_STATUS_ERROR(4, "订单状态错误"),
    ORDER_DETAIL_NOT_EXIST(5, "订单详情不存在"),

    ;

    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

### 5、订单部分测试和总结

在`8081`端口启动(之前在`8080`已经启动了`product`微服务):

<div align="center"><img src="images/2_3.png"></div><br>

也看一下这一部分(`order`微服务)的代码框架:

<div align="center"><img src="images/2_4.png"></div><br>

总结: 服务拆分的方法论:

* 每个微服务都有单独的数据存储；
* 依据服务特点选择不同结构的数据库类型。(MongoDB、MYSQL、ES)
* 难点在确定边界；
* 针对边界设计API；
* 依据边界权衡数据冗余；

## 三、应用通信(远程调用)

有两种通信方式: HTTP和RPC。

HTTP的典型代表: `SpringCloud`。

RPC的典型代表： `Dubbo`。

下面介绍在`SpringCloud`中服务间的两种调用方式: ① RestTemplate，② Feign。

`RestTemplate`和`Feign`之前在`LYDemo`中写过了，这里使用`Feign`进行远程调用。

### 1、完成从order服务到product服务查询商品的远程调用

下面将我们要在订单`order`服务中调用`product`服务的过程利用`Feign`实现。

**注意这个过程**的调用位置:

![2_5.png](images/2_5.png)

这里`order`服务需要调用商品服务`product`来查询商品信息: 传过去`productId`:

于是我们在`product`服务的`Controller`中添加一个专门查询商品，且是专门给订单服务`order`用的方法:

**先看`product`这边的改变**: 

先写`dao`层的方法:

注意只看下面新增的方法：

```java
// 第一个参数是实体，第二个参数是主键的类型
public interface ProductInfoRepository extends JpaRepository<ProductInfo, String> {

    List<ProductInfo> findByProductStatus(Integer productStatus);


    // 后来为订单服务重新写的
    List<ProductInfo> findByProductIdIn(List<String> productIdList);
}
```

然后service层也加了对应的方法 (删除了原来写好的方法):

```java
@Service
public class ProductServiceImpl implements ProductService{

    // Dao层注入到Service层
    @Autowired
    private ProductInfoRepository productInfoRepository;

    // 后来加的
    @Override
    public List<ProductInfo> findList(List<String> productIdList) {
        return productInfoRepository.findByProductIdIn(productIdList);
    }
}
```

最后在`product`服务的`Controller`中增加的方法:

```java
/**
* 获取商品列表(专门给订单服务用的)
* @param productIdList
* @return
*/
@GetMapping("listForOrder")
public List<ProductInfo> listForOrder(List<String>productIdList){
    return productService.findList(productIdList);
}
```

中间对`dap`层的测试: 测试查询`ProductInfoRepository`中新写的方法:`findByProductIdIn`

![2_6.png](images/2_6.png)

下面在`order`服务这边调用那边`product`服务的服务。

只需要利用`Feign`来远程调用即可。

```java
// Feign的配置  底层使用的是动态代理 , 远程调用
@FeignClient(value = "product")
public interface ProductClient {

//    @GetMapping("/product/listForOrder") // 切记切记，不能用GetMapping，因为RequestBody不能用GetMapping
    @PostMapping("/product/listForOrder")  // 注意不要忘记product前缀
    List<ProductInfo> listForOrder(@RequestBody List<String>productIdList); // 用RequestParam就可以用GetMapping
}
```

>  注意加`/product`前缀，以及由于使用了`@RequestBody`，所以只能使用`PostMapping`。																																																																																		

注意这边(`order`)要使用`ProductInfo`，所以在`order`服务这边也要拷贝一份`ProductInfo`。

### 2、扣库存(远程调用)

由于扣库存需要传入一个`list`，且里面有`id`和`count`（商品ID和数量），所以我们又定义了一个`DTO`如下:

```java
// 订单服务 调用商品服务 扣库存的 传入的 json的一个数据转换 dto
@Data
public class CartDTO {

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品数量
     */
    private Integer productQuantity;


    public CartDTO() {
    }

    public CartDTO(String productId, Integer productQuantity) {
        this.productId = productId;
        this.productQuantity = productQuantity;
    }
}
```

先在`product`服务的`ProduceService`中操作：

```java
@Service
public class ProductServiceImpl implements ProductService{
    // 后来加的， 扣库存的
    @Override
    @Transactional // 由于是list，进行事务处理
    public void decreaseStock(List<CartDTO> cartDTOList) {

        for(CartDTO cartDTO : cartDTOList){
            Optional<ProductInfo> productInfoOptional = productInfoRepository.findById(cartDTO.getProductId());
            //判断商品是否存在
            if(!productInfoOptional.isPresent()){ //如果商品不存在 ,抛出一个异常
                throw new ProductException(ResultEnum.PRODUCT_NOT_EXIST);
            }
            // 如果商品存在，还需要判断一下数量
            ProductInfo productInfo = productInfoOptional.get();
            Integer result = productInfo.getProductStock() - cartDTO.getProductQuantity();
            if(result < 0) { //库存不够
                throw new ProductException(ResultEnum.PRODUCT_STOCK_ERROR);
            }

            productInfo.setProductStock(result);
            /**
             * 网上看到一个问题, 但我这里应该没有问题.
             * JpaRepository的save(product)方法做更新操作,更新商品的库存和价格，所以入参的product只
              设置了商品的库存和价格，结果调用完save方法后，除了库存和价格的数据变了，其他字段全部被更新成了null
             */
            productInfoRepository.save(productInfo); // 保存更新
        }
    }
}
```

注意在`ProductServiceImple`的`decreaseStock`中，我还进行了事务处理(`@Transactional`)。

这里面用到一个`ProductException`和一个枚举:

```java
// 库存不够。。。等的异常

import com.zxin.product.enums.ResultEnum;

public class ProductException extends RuntimeException {

    private Integer code;

    public ProductException(Integer code, String message){
        super(message);
        this.code = code;

    }

    public ProductException(ResultEnum resultEnum){
        super(resultEnum.getMessage());
        this.code = resultEnum.getCode();
    }
}
```

```java
@Getter
public enum  ResultEnum {

    PRODUCT_NOT_EXIST(1, "商品不存在"),
    PRODUCT_STOCK_ERROR(2, "库存有误"),
    ;

    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

最后在`ProductController`中加上我们给`order`调用的代码:

```java
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;
    
	// 扣库存(供 order 调用)
    @PostMapping("/decreaseStock")
    public void decreaseStock(@RequestBody List<CartDTO> cartDTOList){
        productService.decreaseStock(cartDTOList);
    }
}
```

然后就是在`order`这边扣库存调用的代码了，也是用了`Feign`: (下面那个方法)

```java
// Feign的配置  底层使用的是动态代理 , 远程调用
@FeignClient(value = "product")
public interface ProductClient {

//    @GetMapping("/product/listForOrder") // 切记切记，不能用GetMapping，因为RequestBody不能用GetMapping
    @PostMapping("/product/listForOrder")  // 注意不要忘记product前缀
    List<ProductInfo> listForOrder(@RequestBody List<String>productIdList); // 用RequestParam就可以用GetMapping


    @PostMapping("/product/decreaseStock")
    void decreaseStock(@RequestBody List<CartDTO> cartDTOList);
}
```

相应的在`order`这边也要有一个`CartDTO`。

### 3、整合接口，打通下单流程

最后我们在`order`的`OrderServiceImpl`这里完成所有的流程，包括：

* 查询商品信息；
* 计算总价；
* 存入库存；
* 订单入库

代码如下

```java
@Service
public class OrderServiceImpl implements OrderService {

    // 这里需要注入两个 Repository

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private OrderMasterRepository orderMasterRepository;

    @Autowired
    private ProductClient productClient; // 需要远程调用

    @Override
    public OrderDTO create(OrderDTO orderDTO) {

        /**
         *
         2、查询商品信息 (需要调用远程服务 Feign)；
         3、计算总价；
         4、扣库存(调用商品服务)；
         5、订单入库；  (这个可以做)
         */
        String orderId = KeyUtil.generateUniqueKey();

        //  2、查询商品信息 (需要调用远程服务 Feign)；
        List<String> productIdList = orderDTO.getOrderDetailList().stream()
                .map(OrderDetail::getProductId)
                .collect(Collectors.toList());
        List<ProductInfo> productInfoList = productClient.listForOrder(productIdList);//需要传递ProductId <List>

        // 3、计算总价；
        BigDecimal orderAmount = new BigDecimal(BigInteger.ZERO);
        for(OrderDetail orderDetail : orderDTO.getOrderDetailList()){

            // 和查询到的商品的单价
            for(ProductInfo productInfo : productInfoList){
                // 判断相等才计算
                if(productInfo.getProductId().equals(orderDetail.getProductId())){
                    // 单价 productInfo.getProductPrice()
                    // 数量 orderDetail.getProductQuantity(
                    orderAmount = productInfo.getProductPrice()
                            .multiply(new BigDecimal(orderDetail.getProductQuantity()))
                            .add(orderAmount);

                    //给订单详情赋值
                    BeanUtils.copyProperties(productInfo, orderDetail); //这种拷贝要注意值为null也会拷贝
                    orderDetail.setOrderId(orderId);
                    orderDetail.setDetailId(KeyUtil.generateUniqueKey()); // 每个Detail的Id

                    // 订单详情入库
                    orderDetailRepository.save(orderDetail);
                }
            }
        }

        // 4、扣库存(调用商品服务)；
        List<CartDTO> cartDTOList = orderDTO.getOrderDetailList().stream()
                .map(e -> new CartDTO(e.getProductId(), e.getProductQuantity()))
                .collect(Collectors.toList());
        productClient.decreaseStock(cartDTOList); //订单主库入库了

        // 5、订单入库 (OrderMaster)
        OrderMaster orderMaster = new OrderMaster();
        orderDTO.setOrderId(orderId); // 生成唯一的订单逐渐(订单号)
        BeanUtils.copyProperties(orderDTO, orderMaster); // 将orderDTO拷贝到orderMaster
        orderMaster.setOrderAmount(orderAmount); // 设置订单总金额
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());

        orderMasterRepository.save(orderMaster); //存在主仓库
        return orderDTO;
    }
}

```

测试:

更改前:

![image.png](images/2_7.png)

然后使用postman对`order` (8081端口)发送`/order/create`请求:

![2_8.png](images/2_8.png)

最后数据库的更改:

![2_9.png](images/2_9.png)

