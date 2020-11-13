/*
    测试循环Map
*/
var map = {
    key1 : 1,
    key2 : 2,
    key3 : 3
};
var sum = 0;
var keys = '';
for(key,value in map){
    sum = sum + value;
    keys = keys + key
}
return keys + '-' + sum;