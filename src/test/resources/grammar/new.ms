import 'java.util.Date' as date;
import 'java.text.SimpleDateFormat' as sdf;
var now = new date();
var df = new sdf('yyyy-MM-dd');
return df.format(now);