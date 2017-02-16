package com.vunke.sharehome.model;

public class BuyTrafficBean {

    /**
     * msg : 本地网编号
     * ret : 1001
     * sign : FA360DFF8D15E35B4F861EB43467F2DD
     * timestamp : 20161011123055
     */

    private ResultBean result;
    /**
     * result : {"msg":"本地网编号","ret":"1001","sign":"FA360DFF8D15E35B4F861EB43467F2DD","timestamp":"20161011123055"}
     * code : 400
     * message : 订购失败
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
        private String ret;
        private String sign;
        private String timestamp;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
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