package com.kxy.auth;

/**
 * Created by zhuxi on 2016/11/1.
 */
public class StartInfoBean {

    /**
     * createTime : {"date":28,"day":5,"hours":0,"minutes":0,"month":9,"seconds":0,"time":1477584000000,"timezoneOffset":-480,"year":116}
     * id : 1102
     * jsonData :
     * remark :
     * startActivity : com.funhotel.iptv.ui.MainActivity
     * startPackage : com.funhotel.iptv
     * userGroupId : 2a000142
     */

    private DataBean data;
    /**
     * data : {"createTime":{"date":28,"day":5,"hours":0,"minutes":0,"month":9,"seconds":0,"time":1477584000000,"timezoneOffset":-480,"year":116},"id":"1102","jsonData":"","remark":"","startActivity":"com.funhotel.iptv.ui.MainActivity","startPackage":"com.funhotel.iptv","userGroupId":"2a000142"}
     * code : 200
     * message : success
     */

    private String code;
    private String message;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class DataBean {
        /**
         * date : 28
         * day : 5
         * hours : 0
         * minutes : 0
         * month : 9
         * seconds : 0
         * time : 1477584000000
         * timezoneOffset : -480
         * year : 116
         */

        private CreateTimeBean createTime;
        private String id;
        private String jsonData;
        private String remark;
        private String startActivity;
        private String startPackage;
        private String userGroupId;

        public CreateTimeBean getCreateTime() {
            return createTime;
        }

        public void setCreateTime(CreateTimeBean createTime) {
            this.createTime = createTime;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getJsonData() {
            return jsonData;
        }

        public void setJsonData(String jsonData) {
            this.jsonData = jsonData;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getStartActivity() {
            return startActivity;
        }

        public void setStartActivity(String startActivity) {
            this.startActivity = startActivity;
        }

        public String getStartPackage() {
            return startPackage;
        }

        public void setStartPackage(String startPackage) {
            this.startPackage = startPackage;
        }

        public String getUserGroupId() {
            return userGroupId;
        }

        public void setUserGroupId(String userGroupId) {
            this.userGroupId = userGroupId;
        }

        public static class CreateTimeBean {
            private int date;
            private int day;
            private int hours;
            private int minutes;
            private int month;
            private int seconds;
            private long time;
            private int timezoneOffset;
            private int year;

            public int getDate() {
                return date;
            }

            public void setDate(int date) {
                this.date = date;
            }

            public int getDay() {
                return day;
            }

            public void setDay(int day) {
                this.day = day;
            }

            public int getHours() {
                return hours;
            }

            public void setHours(int hours) {
                this.hours = hours;
            }

            public int getMinutes() {
                return minutes;
            }

            public void setMinutes(int minutes) {
                this.minutes = minutes;
            }

            public int getMonth() {
                return month;
            }

            public void setMonth(int month) {
                this.month = month;
            }

            public int getSeconds() {
                return seconds;
            }

            public void setSeconds(int seconds) {
                this.seconds = seconds;
            }

            public long getTime() {
                return time;
            }

            public void setTime(long time) {
                this.time = time;
            }

            public int getTimezoneOffset() {
                return timezoneOffset;
            }

            public void setTimezoneOffset(int timezoneOffset) {
                this.timezoneOffset = timezoneOffset;
            }

            public int getYear() {
                return year;
            }

            public void setYear(int year) {
                this.year = year;
            }
        }
    }
}
