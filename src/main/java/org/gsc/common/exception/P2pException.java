package org.gsc.common.exception;

public class P2pException extends GscException {

    private TypeEnum type;

    public P2pException(TypeEnum type, String errMsg){
        super(errMsg);
        this.type = type;
    }

    public TypeEnum getType() {
        return type;
    }

    public enum TypeEnum {
        NO_SUCH_MESSAGE                         (1, "No such message"),
        PARSE_MESSAGE_FAILED                    (2, "Parse message failed"),
        DEFAULT                                 (100, "default exception");

        private Integer value;
        private String desc;

        TypeEnum(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public String toString(){
            return value + ", " + desc;
        }
    }
    
}