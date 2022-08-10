```java
public class Demo {
    // 插入
    var user = new User();
    user.setId(1L);
    user.setName("name");
    session.persist(user);
    // 修改
    user.setName("new name");
    session.merge(user);
    
    // 删除
    session.remove(user);
    
    // 原生sql 查询
    session.createNativeQuery("select * from user",User.class).list();

    // hql 查询（格式像是 sql 混杂 java）
    String hql = "from User s where s.name = :name";
    session.createQuery(hql,User .class).setParameter("name","...").list();

    String hql2 = "from Work w where w.user.name = :name";
    session.createQuery(hql2,Work .class).setParameter("name","...").list();

    // criteria 查询（纯 java 代码的方式构造 sql）我推荐这种，不过用起来比较复杂
    var builder = session.getCriteriaBuilder();

    var query = builder.createQuery(User.class);
    var root = query.from(User.class);
    query.select(root);
    query.where(builder.between(root.get("id"),0L,1000L));

    var list = session.createQuery(query).list();

    var query2 = builder.createQuery(Work.class);
    var root2 = query.from(Work.class);
    query2.select(root2);
    query2.where(builder.between(root2.get("user").

    get("id"), 0L,1000L));

    var list2 = session.createQuery(query).list();
}
```

* 完成群组管理 - 1.8.12
* 完成定时任务管理 - 1.9.12
* 修复一系列bug - 1.9.13
* 更改pluginData的类为kt，修复群消息的bug - 1.9.14
* 兼容了一下旧版回复数据 - 1.9.15
* 修复定时不存时不提示只报错 - 1.9.16
* 添加了定时器的删除 -1.9.17
* 修正了定时器的miraiCode识别 - 1.9.18
* 修正了旧数据兼容问题 - 1.9.19
* 修正了当被禁言是发不出消息报错 - 1.9.20
* 修正了定时器删除不识别中文冒号 - 1.9.21
* 添加定时器查询功能 - 1.10.21
* 添加违禁词功能 - 1.11.21
* 修正消息发送失败的提示 -1.11.22
* 修正一些操作失败时的报错 - 1.11.23
* 修正违禁词格式能通过问题 - 1.11.24