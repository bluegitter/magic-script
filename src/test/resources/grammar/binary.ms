/*
    各种操作符测试
*/
import 'java.lang.System' as system;
var a = 1;
var b = 2;
system.out.println("a = " + a + ", b = " + b);
var c = a + b;
system.out.println("a + b = " + c);
c = a - b;
system.out.println("a - b = " + c);
c = a * b;
system.out.println("a * b = " + c);
c = a / b;
system.out.println("a / b = " + c);
c = a % b;
system.out.println("a % b = " + c);
c = a > b;
system.out.println("a > b = " + c);
c = a >= b;
system.out.println("a >= b = " + c);
c = a == b;
system.out.println("a == b = " + c);
c = a < b;
system.out.println("a < b = " + c);
c = a <= b;
system.out.println("a <= b = " + c);
c = a != b;
system.out.println("a != b = " + c);
var d = true;
system.out.println("!d= " + !d);
var list = [0,1,2];
list[2] = 100;
system.out.println("list[2]=100,list= " + list);
var map = {};
map.xx = 123;
system.out.println("map.xx = 123,map= " + map);
map['xx'] = 456;
system.out.println("map['xx'] = 456,map= " + map);