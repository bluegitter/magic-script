var t = System.currentTimeMillis();
var sum = 0;
for(val in range(0,100000000)){
    sum = sum + val;
}
return sum + "   " + (System.currentTimeMillis() - t);