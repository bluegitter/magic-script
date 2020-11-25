/*
    ...测试
*/

// ...list

var i = 0;
var fun = ()=>{
    return [1,2,3];
}
var list = [3,2,1]
var future = async () => [1,2,3];
var twoDimensional = [[4,5,6]];

var r = []
r.addAll([...[1,2,3],9,6]);
r.addAll([...list,9,6]);
r.addAll([...fun(),9,6]);
r.addAll([...future.get(),9,6]);

// ...map

var map = {key1:1,key2:2}

var map2 = {key0:0, ...map, key3:3, key4:4}

return r == [1, 2, 3, 9, 6, 3, 2, 1, 9, 6, 1, 2, 3, 9, 6, 1, 2, 3, 9, 6] && map2 == {key0:0, key1:1, key2:2, key3:3, key4:4}

