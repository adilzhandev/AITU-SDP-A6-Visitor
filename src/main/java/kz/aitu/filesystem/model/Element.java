package kz.aitu.filesystem.model;

import kz.aitu.filesystem.visitor.Visitor;

public interface Element {

    String getName();

    void accept(Visitor visitor);
}
