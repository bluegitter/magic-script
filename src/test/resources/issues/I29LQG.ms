// https://gitee.com/ssssssss-team/magic-api/issues/I29LQG
import 'org.ssssssss.script.MagicScriptContext' as context;
var test = (obj) => {
    System.out.println(obj);
    return context.get().eval('obj.name');
}
return test({name : 'xxx'}) == 'xxx'