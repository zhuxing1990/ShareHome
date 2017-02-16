package com.vunke.sharehome.model;


/**
 * Created by zhuxi on 2016/10/8.
 */
public class DnwSmsOrderBean {

    /**
     * msg : 发送成功
     * order_no : 16100817574970625140
     * ret : 0
     * sign : 8A5ED9E09DCD90F581F002BB6C74348C
     * timestamp : 20161008175948
     */

    private ResultBean result;
    /**
     * result : {"msg":"发送成功","order_no":"16100817574970625140","ret":"0","sign":"8A5ED9E09DCD90F581F002BB6C74348C","timestamp":"20161008175948"}
     * code : 200
     * message : 发送成功
     */

    private String code;
    private String message;

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
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

    public static class ResultBean {
        private String msg;
        private String order_no;
        private String ret;
        private String sign;
        private String timestamp;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getOrder_no() {
            return order_no;
        }

        public void setOrder_no(String order_no) {
            this.order_no = order_no;
        }

        public String getRet() {
            return ret;
        }

        public void setRet(String ret) {
            this.ret = ret;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
