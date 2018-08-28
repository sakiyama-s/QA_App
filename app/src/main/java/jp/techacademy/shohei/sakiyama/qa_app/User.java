package jp.techacademy.shohei.sakiyama.qa_app;

import java.util.ArrayList;

public class User {
    private String user;
    private ArrayList<String> favsArrayList;

    public User(String user){
        this.user = user;

        // お気に入り登録リストをnullにしないため、最初の要素に空を追加
        ArrayList<String> list = new ArrayList<>();
        list.add("");
        this.favsArrayList = list;

    }

    public ArrayList<String> getFavsArrayList(){
        return this.favsArrayList;
    }

}
