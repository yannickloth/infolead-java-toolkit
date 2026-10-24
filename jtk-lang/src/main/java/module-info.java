module eu.infolead.jtk {
    requires static com.fasterxml.jackson.annotation;
    requires static jakarta.annotation;

    exports eu.infolead.jtk.anomaly;
    exports eu.infolead.jtk.anomaly.http;
    exports eu.infolead.jtk.fp;
    exports eu.infolead.jtk.fp.either;
    exports eu.infolead.jtk.fp.tailrec;
    exports eu.infolead.jtk.fp.validation;
    exports eu.infolead.jtk.http;
    exports eu.infolead.jtk.lang;
    exports eu.infolead.jtk.logic;
    exports eu.infolead.jtk.organisms;
    exports eu.infolead.jtk.rfc;
    exports eu.infolead.jtk.time;
}
