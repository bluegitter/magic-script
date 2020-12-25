var result =
    select
       x.a,
       count(x.a) count
    from [{a:1,b:2,c:3},{a:1,b:5,c:6}] x
    join [1,2,3] on 1=2
    group by x.a

result.each(it => System.out.println(it))
