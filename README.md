# magic-script

## 介绍
`magic-script`是一款基于JVM的脚本语言，目前主要是为`magic-api`项目设计。

## 应用案例
- [magic-api，接口快速开发框架，通过Web页面配置，自动映射为HTTP接口](https://gitee.com/ssssssss-team/magic-api)
- [spider-flow，新一代爬虫平台，以图形化方式定义爬虫流程，不写代码即可完成爬虫](https://gitee.com/ssssssss-team/spider-flow)
## 脚本语法

### 关键字
<table>
    <thead>
        <tr>
            <th>关键字</th>
            <th>含义</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>var</td>
            <td>定义变量</td>
        </tr>
        <tr>
            <td>if</td>
            <td>用在条件语句中，表明当条件不成立时的分支</td>
        </tr>
        <tr>
            <td>for</td>
            <td>循环语句</td>
        </tr>
        <tr>
            <td>in</td>
            <td>与 for 配合使用</td>
        </tr>
        <tr>
            <td>continue</td>
            <td>执行下一次循环</td>
        </tr>
        <tr>
            <td>break</td>
            <td>跳出循环</td>
        </tr>
        <tr>
            <td>return</td>
            <td>终止当前过程的执行并正常退出到上一个执行过程中</td>
        </tr>
        <tr>
            <td>try</td>
            <td>用于捕获可能发生异常的代码块</td>
        </tr>
        <tr>
            <td>catch</td>
            <td>与 try 关键字配合使用，当发生异常时执行</td>
        </tr>
        <tr>
            <td>finally</td>
            <td>与 try 关键字配合使用，finally 块无论发生异常都会执行</td>
        </tr>
        <tr>
            <td>import</td>
            <td>导入 Java 类或导入已定义好的模块</td>
        </tr>
        <tr>
            <td>as</td>
            <td>与 import 关键字配合使用，用作将导入的 Java类或模块 命名为一个本地变量名</td>
        </tr>
        <tr>
            <td>new</td>
            <td>创建对象</td>
        </tr>
        <tr>
            <td>true</td>
            <td>基础类型之一，表示 Boolean 的：真值</td>
        </tr>
        <tr>
            <td>false</td>
            <td>基础类型之一，表示 Boolean 的：假值</td>
        </tr>
        <tr>
            <td>null</td>
            <td>基础类型之一，表示 NULL 值</td>
        </tr>
        <tr>
            <td>async</td>
            <td>异步调用</td>
        </tr>
    </tbody>
</table>

### 运算符
<table>
    <thead>
        <tr>
            <th colspan="2">数学运算</th>
            <th colspan="2">比较运算</th>
            <th colspan="2">逻辑运算</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>+</td>
            <td>加法</td>
            <td>&lt;</td>
            <td>小于</td>
            <td>&&</td>
            <td>并且</td>
        </tr>
        <tr>
            <td>-</td>
            <td>减法</td>
            <td>&lt;=</td>
            <td>小于等于</td>
            <td>||</td>
            <td>或者</td>
        </tr>
        <tr>
            <td>*</td>
            <td>乘法</td>
            <td>&gt;</td>
            <td>大于</td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td>/</td>
            <td>除法</td>
            <td>&gt;=</td>
            <td>大于等于</td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td>%</td>
            <td>取模</td>
            <td>==</td>
            <td>等于</td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td></td>
            <td></td>
            <td>!=</td>
            <td>不等于</td>
            <td></td>
            <td></td>
        </tr>
    </tbody>
</table>

### 类型
<table>
    <thead>
        <tr>
            <th>类型</th>
            <th>写法</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>byte</td>
            <td>`123b`、`123B`</td>
        </tr>
        <tr>
            <td>short</td>
            <td>`123s`、`123S`</td>
        </tr>
        <tr>
            <td>int</td>
            <td>`123`</td>
        </tr>
        <tr>
            <td>long</td>
            <td>`123l`、`123L`</td>
        </tr>
        <tr>
            <td>float</td>
            <td>`123f`、`123F`</td>
        </tr>
        <tr>
            <td>double</td>
            <td>`123d`、`123D`</td>
        </tr>
        <tr>
            <td>BigDecimal</td>
            <td>`123m`、`123M`</td>
        </tr>
        <tr>
            <td>boolean</td>
            <td>`true`、`false`</td>
        </tr>
        <tr>
            <td>string</td>
            <td>`'hello'`</td>
        </tr>
        <tr>
            <td>string</td>
            <td>`"hello"`</td>
        </tr>
        <tr>
            <td>string</td>
            <td>`"""多行文本块,主要用于编写SQL"""`</td>
        </tr>
        <tr>
            <td>lambda</td>
            <td>`()=>expr`、`(param1,param2....)=>{...}`</td>
        </tr>
        <tr>
            <td>list</td>
            <td>`[1,2,3,4,5]`</td>
        </tr>
        <tr>
            <td>map</td>
            <td>{key : value,key1 : value}</td>
        </tr>
        <tr>
            <td colspan="2">{\$key : "value"}//$key表示动态从变量中获取key值</td>
        </tr>
    </tbody>
</table>

### 一元运算符

您可以通过一元运算`-`符将数字取反，例如`-234`。要取反布尔表达式，可以使用`!`运算符，例如`!true`。
自增/自减 `i++` 、 `++i`、`i--`、`--i`

### 算术运算符

支持常见的算术运算符，例如`1 + 2 * 3 / 4 % 2`、同样也支持`+=`、`-=`、`*=`、`/=`、`%=`

### 比较运算符

`23 < 34`，`23 <= 34`，`23 > 34`，`23 >= 34`，`true != false`，`23 == 34`

比较运算符结果为`boolean`类型

### 逻辑运算符

除了一元运算`!`符，您还可以使用`&&`和`||`。就像Java中一样，运算符也是一种短路运算符。如果`&&`左边计算为`false`，则不会计算右边。如果`||`左侧为true，则不会计算右边
在1.3.0+版本中增强了`&&` `||` 不在强制两边必须是布尔类型。作用与`JS`一样

### 三元运算符

三元运算符是`if`语句的简写形式，其工作方式类似于Java中，例如`true ? "yes" : "no"`
在1.2.7+版本中，增强了`if` 和三元运算符，不在强制值必须是布尔类型，可以写`if(xxx)`的形式当`xxx`为以下情况时为`fasle`、其它情况为`true`
- `null`
- 空集合
- 空Map
- 空数组
- 数值==0
- 非空字符串
- `false`


### for循环

当前for循环只支持两种，循环集合或Map

#### 循环集合
```javascript
import 'java.lang.System' as System;
var list = [1,2,3];
for(index,item in list){    //如果不需要index，也可以写成for(item in list)
    System.out.println(index + ":" + item);
}
/*
结果：
0:1
1:2
2:3
*/
```

#### 循环指定次数
```javascript
var sum = 0;
for(value in range(0,100)){    //包括0包括100
    sum = sum + value; //不支持+= -= *= /= ++ -- 这种运算
}
return sum;
/*
结果：5050
*/
```

#### 循环map
```javascript
import 'java.lang.System' as System;
var map = {
    key1 : 123,
    key2 : 456
};
for(key,value in map){    //如果不需要key，也可以写成for(value in map)
    System.out.println(key + ":" + value);
}
/*
结果：
key1:123
key2:456
*/
```

### Import导入

#### 导入Java类
```javascript
import 'java.lang.System' as system;//导入静态类并赋值给system作为变量
import 'javax.sql.DataSource' as ds;//从spring中获取DataSource并将值赋值给ds作为变量
import 'org.apache.commons.lang3.StringUtils' as string;//导入静态类并赋值给ds作为变量

System.out.println('调用System打印');//调用静态方法
System.out.println(ds);
System.out.println(string.isBlank('')); //调用静态方法
```

### new创建对象

#### 创建对象
```javascript
import 'java.util.Date' as Date;//创建之前先导包，不支持.*的操作
return new Date();
```

#### 导入已定义的模块
```javascript
import log; //导入log模块，并定义一个与模块名相同的变量名
//import log as logger; //导入log模块，并赋值给变量 logger
log.info('Hello {}','Magic API!')
```

### 异步调用

#### 异步调用方法
```javascript
var val = async db.select('.....'); // 异步调用，返回Future类型
return val.get();   //调用Future的get方法
```

#### 异步调用lambda
```javascript
var list = [];
for(index in range(1,10)){
    list.add(async (index)=>db.selectInt('select #{index}'));
}
return list.map(item=>item.get());  // 循环获取结果
```