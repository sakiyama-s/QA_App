package jp.techacademy.shohei.sakiyama.qa_app;

import android.content.Intent;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavoriteListActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFavoriteRef;
    private DatabaseReference mQuestionIdRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;
    private String mQuestionId;
    //private int mGenre;
    private boolean onCreateFlag = false; // 別のactivityから遷移してきたときにリストを再更新するための管理フラグ


    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {


            HashMap map = (HashMap) dataSnapshot.getValue();

            mQuestionId = (String) map.get("questionId");
            String tmp = (String) map.get("genre");
            final int mGenre = Integer.parseInt(tmp);

            mQuestionIdRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre)).child(mQuestionId);
            mQuestionIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap map = (HashMap) dataSnapshot.getValue();
                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String uid = (String) map.get("uid");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        // 別のactivityに遷移するときフラグ初期化
        onCreateFlag = false;
    }

    @Override
    protected void onResume() {
        super.onResume();


        // userがnullだったらMainActivityまで戻す
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            finish();

        }else{
            // お気に入り一覧画面でお気に入り解除してこのアクティビティに戻ってきたときのための処理
            // その場合、onCreateFlagはfalseになっているはず
            if (onCreateFlag == true) {
                return;
            } else {
                mQuestionArrayList.clear();
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                mListView.setAdapter(mAdapter);

                // リスナーの二重登録を防ぐ
                if(mFavoriteRef!=null){
                    mFavoriteRef.removeEventListener(mFavoriteEventListener);
                }

                mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                user = FirebaseAuth.getInstance().getCurrentUser();
                mFavoriteRef = mDatabaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritesPATH);

                Log.d("sa-ki", "mFavoriteRef --> "+ mFavoriteRef.toString());
                // TODO お気に入りリストが空だったらこのアクティビティを終えてMainActivityに戻る。finish()
//                if(mFavoriteRef==null){
//                    Log.d("sa-ki", "mFavoriteRefがnullだったのでmainActivityに戻ります");
//                    finish();
//
//                }
                mFavoriteRef.addChildEventListener(mFavoriteEventListener);
            }
        }



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        //
        onCreateFlag = true;

        setTitle("お気に入り");

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });


        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mFavoriteRef = mDatabaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritesPATH);
        mFavoriteRef.addChildEventListener(mFavoriteEventListener);
    }


}
