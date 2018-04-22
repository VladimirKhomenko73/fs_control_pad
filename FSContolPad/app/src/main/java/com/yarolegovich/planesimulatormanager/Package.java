package com.yarolegovich.planesimulatormanager;

import android.util.Log;

/**
 * Created by yarolegovich on 28.05.2015.
 * Модель пакета, используемая в связке с библиотекой Gson в классе JsonUtil
 * Builder для пакета
 */
public class Package {

    private Head h;
    private Body p;
    public Package() {
        h = new Head();
        p = new Body();
    }

    public Body getBody() {
        return p;
    }

    public boolean isValid(String source, String dest) {
        return source.equals(h.s) && dest.equals(h.d);
    }

    public static class Head {
        private String s;
        private String d;
        private int t;
    }

    public static class Body {
        private String com;
        private String mes;

        public String getMes() {
            return mes;
        }
    }

    public static class PackageBuilder {
        private Package instance;
        public PackageBuilder() {
            instance = new Package();
        }
        public PackageBuilder source(String source) {
            instance.h.s = source;
            return this;
        }
        public PackageBuilder destination(String destination) {
            instance.h.d = destination;
            return this;
        }
        public PackageBuilder flyNumber(int flyNumber) {
            instance.h.t = flyNumber;
            return this;
        }
        public PackageBuilder command(String command) {
            instance.p.com = command;
            return this;
        }
        public PackageBuilder message(String message) {
            instance.p.mes = message;
            return this;
        }
        public Package build() {
            return instance;
        }
    }
}
