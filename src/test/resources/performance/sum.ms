var t = System.currentTimeMillis();
var sum = 0L;
for(val in range(0,10000000)){
    sum = sum + val;
}
return sum + "   " + (System.currentTimeMillis() - t);