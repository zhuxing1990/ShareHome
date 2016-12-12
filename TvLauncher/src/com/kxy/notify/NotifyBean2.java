package com.kxy.notify;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuxi on 2016/10/27.
 */
public class NotifyBean2 {

    /**
     * account : 073100009999@VOD
     * deadline : 1365152930
     * money : 15
     * push_id : 1
     * push_type : 1
     * strategy_id : 3
     */

    private List<DataBean> data;
    private int RxBuscode;
    private String UserId;
    
    public String getUserId() {
		return UserId;
	}
	public void setUserId(String userId) {
		UserId = userId;
	}

	public int getRxBuscode() {
		return RxBuscode;
	}

	public void setRxBuscode(int rxBuscode) {
		RxBuscode = rxBuscode;
	}

	public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        private String account;
        private int deadline;
        private String money;
        private int push_id;
        private int push_type;
        private int strategy_id;
        private String business_name;
        private long consume_time;
        private String productname;
        private String check_code;
        private String img_url_1;
        private String img_url_2;
        private String img_link_1;
        private String img_link_2;
        private String video_channel_id;
        private String video_content_id;
        private String video_link;
        private String notice;
        private String special_url;
        private String subtitle;
        private String adslName;
        private int busiType;
        private int cityCode;
        private long mobilePhone;
        private String orderTime;
        private String saleName;
        private String userName;
        private int webOrder;
        private int skip_time;
        private String version_code;
        private String business_type;
        private ArrayList<ProductAttr> productAttr;
      
        
        @Override
		public String toString() {
			return "DataBean [account=" + account + ", deadline=" + deadline
					+ ", money=" + money + ", push_id=" + push_id
					+ ", push_type=" + push_type + ", strategy_id="
					+ strategy_id + ", business_name=" + business_name
					+ ", consume_time=" + consume_time + ", productname="
					+ productname + ", check_code=" + check_code
					+ ", video_channel_id=" + video_channel_id
					+ ", video_content_id=" + video_content_id
					+ ", video_link=" + video_link + ", notice=" + notice
					+ ", special_url=" + special_url + ", subtitle=" + subtitle
					+ ", adslName=" + adslName + ", busiType=" + busiType
					+ ", cityCode=" + cityCode + ", mobilePhone=" + mobilePhone
					+ ", orderTime=" + orderTime + ", saleName=" + saleName
					+ ", userName=" + userName + ", webOrder=" + webOrder
					+ ", skip_time=" + skip_time + ", version_code="
					+ version_code + ", business_type=" + business_type
					+ ", productAttr=" + productAttr + "]";
		}
        
		public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getAdslName() {
            return adslName;
        }

        public void setAdslName(String adslName) {
            this.adslName = adslName;
        }

        public String getBusiness_name() {
            return business_name;
        }

        public void setBusiness_name(String business_name) {
            this.business_name = business_name;
        }

        public int getBusiType() {
            return busiType;
        }

        public void setBusiType(int busiType) {
            this.busiType = busiType;
        }

        public String getCheck_code() {
            return check_code;
        }

        public void setCheck_code(String check_code) {
            this.check_code = check_code;
        }

        public int getCityCode() {
            return cityCode;
        }

        public void setCityCode(int cityCode) {
            this.cityCode = cityCode;
        }

        public long getConsume_time() {
            return consume_time;
        }

        public void setConsume_time(long consume_time) {
            this.consume_time = consume_time;
        }

        public int getDeadline() {
            return deadline;
        }

        public void setDeadline(int deadline) {
            this.deadline = deadline;
        }

        public long getMobilePhone() {
            return mobilePhone;
        }

        public void setMobilePhone(long mobilePhone) {
            this.mobilePhone = mobilePhone;
        }

        public String getMoney() {
            return money;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public String getNotice() {
            return notice;
        }

        public void setNotice(String notice) {
            this.notice = notice;
        }

        public String getOrderTime() {
            return orderTime;
        }

        public void setOrderTime(String orderTime) {
            this.orderTime = orderTime;
        }

        public ArrayList<ProductAttr> getProductAttr() {
            return productAttr;
        }

        public void setProductAttr(ArrayList<ProductAttr> productAttr) {
            this.productAttr = productAttr;
        }

        public String getProductname() {
            return productname;
        }

        public void setProductname(String productname) {
            this.productname = productname;
        }

        public int getPush_id() {
            return push_id;
        }

        public void setPush_id(int push_id) {
            this.push_id = push_id;
        }

        public int getPush_type() {
            return push_type;
        }

        public void setPush_type(int push_type) {
            this.push_type = push_type;
        }

        public String getSaleName() {
            return saleName;
        }

        public void setSaleName(String saleName) {
            this.saleName = saleName;
        }

        public String getSpecial_url() {
            return special_url;
        }

        public void setSpecial_url(String special_url) {
            this.special_url = special_url;
        }

        public int getStrategy_id() {
            return strategy_id;
        }

        public void setStrategy_id(int strategy_id) {
            this.strategy_id = strategy_id;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
        
        public String getImg_url_1() {
			return img_url_1;
		}

		public void setImg_url_1(String img_url_1) {
			this.img_url_1 = img_url_1;
		}

		public String getImg_url_2() {
			return img_url_2;
		}

		public void setImg_url_2(String img_url_2) {
			this.img_url_2 = img_url_2;
		}

		public String getImg_link_1() {
			return img_link_1;
		}

		public void setImg_link_1(String img_link_1) {
			this.img_link_1 = img_link_1;
		}

		public String getImg_link_2() {
			return img_link_2;
		}

		public void setImg_link_2(String img_link_2) {
			this.img_link_2 = img_link_2;
		}

		public String getVideo_channel_id() {
            return video_channel_id;
        }

        public void setVideo_channel_id(String video_channel_id) {
            this.video_channel_id = video_channel_id;
        }

        public String getVideo_content_id() {
            return video_content_id;
        }

        public void setVideo_content_id(String video_content_id) {
            this.video_content_id = video_content_id;
        }

        public String getVideo_link() {
            return video_link;
        }

        public void setVideo_link(String video_link) {
            this.video_link = video_link;
        }

        public int getWebOrder() {
            return webOrder;
        }

        public void setWebOrder(int webOrder) {
            this.webOrder = webOrder;
        }
        

        public int getSkip_time() {
			return skip_time;
		}

		public void setSkip_time(int skip_time) {
			this.skip_time = skip_time;
		}
		

		public String getVersion_code() {
			return version_code;
		}

		public void setVersion_code(String version_code) {
			this.version_code = version_code;
		}

		public String getBusiness_type() {
			return business_type;
		}

		public void setBusiness_type(String business_type) {
			this.business_type = business_type;
		}


		public class ProductAttr {
            private String attrCode;
            private String attrName;
            private String attrValue;

            public String getAttrCode() {
                return attrCode;
            }

            public void setAttrCode(String attrCode) {
                this.attrCode = attrCode;
            }

            public String getAttrName() {
                return attrName;
            }

            public void setAttrName(String attrName) {
                this.attrName = attrName;
            }

            public String getAttrValue() {
                return attrValue;
            }

            public void setAttrValue(String attrValue) {
                this.attrValue = attrValue;
            }


        }
    }
}
