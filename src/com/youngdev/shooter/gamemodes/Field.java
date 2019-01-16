package com.youngdev.shooter.gamemodes;

import java.util.List;

public class Field {

    public int type;
    public static final int TYPE_CHOICE = 0;
    public static final int TYPE_STRING = 1;
    public static final int TYPE_INT = 2;
    public static final int TYPE_DOUBLE = 3;

    // Usable only with TYPE_CHOICE
    private List<String> options;

    public Field(int type) {
        this.type = type;
    }

    public Field(int type, List<String> options) {
        this.type = type;
        this.options = options;
    }

    public Value getValue(String data) {
        return new Value(data, type);
    }

    public List<String> getOptions() {
        return options;
    }

    public class Value {
        String stringValue;
        int intValue;
        double doubleValue;
        int choiceIndex;

        private Value(String data, int type) {
            switch (type) {
                case TYPE_CHOICE:
                    choiceIndex = options.indexOf(data);
                    break;
                case TYPE_INT:
                    intValue = Integer.parseInt(data);
                    break;
                case TYPE_DOUBLE:
                    doubleValue = Double.parseDouble(data);
                    break;
                case TYPE_STRING:
                    stringValue = data;
                    break;
            }
        }

        public String getStringValue() {
            if(type != TYPE_STRING) return null;
            return stringValue;
        }

        public int getIntValue() {
            if(type != TYPE_INT) return 0;
            return intValue;
        }

        public double getDoubleValue() {
            if(type != TYPE_DOUBLE) return 0;
            return doubleValue;
        }

        public int getChoiceIndex() {
            if(type != TYPE_CHOICE) return -1;
            return choiceIndex;
        }
    }
}
