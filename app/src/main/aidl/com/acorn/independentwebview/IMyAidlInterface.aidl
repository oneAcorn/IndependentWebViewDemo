// IMyAidlInterface.aidl
package com.acorn.independentwebview;
import com.acorn.independentwebview.Book;
// Declare any non-default types here with import statements

interface IMyAidlInterface {

    String myAction(String msg);

    List<Book> requestBookList();

    void addBookInOut(inout Book book);

    void addBookIn(in Book book);

    void addBookOut(out Book book);
}
