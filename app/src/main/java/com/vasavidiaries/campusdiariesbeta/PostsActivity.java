package com.vasavidiaries.campusdiariesbeta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.vasavidiaries.campusdiariesbeta.Models.Posts;

public class PostsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    StringBuffer buffer = new StringBuffer();
    ListView lvPosts;
    DrawerLayout mDrawerlayout;
    ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);
        Log.d("PLACE","IN POSTS ACTIVITY");

        mDrawerlayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerlayout, R.string.open, R.string.close);
        mDrawerlayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setNavigationViewListner();

        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                                             .cacheInMemory(true)
                                             .cacheOnDisk(true)
                                             .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                                         .defaultDisplayImageOptions(defaultOptions)
                                         .build();
        ImageLoader.getInstance().init(config);

        lvPosts = (ListView)findViewById(R.id.lvPosts);

        new GetPosts().execute("Execute");
    }

    private void setNavigationViewListner() {
        Log.d("NavLayout","in");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Log.d("NavLayout","out");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        Log.d("NavItems","in");
        switch(item.getItemId())
        {

            case R.id.dNewpost:{
                SharedPreferences prefs = getSharedPreferences("logininfo", Context.MODE_PRIVATE);
                if(prefs.getString("username","").equals(""))
                    Toast.makeText(getApplicationContext(),"Log in to post.",Toast.LENGTH_SHORT).show();
                else{
                    Intent gotonewpost = new Intent(this, NewPostActivity.class);
                    startActivity(gotonewpost);
                }
                break;
            }

            case R.id.dModerate:{

                SharedPreferences prefs = getSharedPreferences("logininfo", Context.MODE_PRIVATE);

                if(prefs.getString("ismoderator","").equals("true")) {
                    Toast.makeText(getApplicationContext(),"Moderator status confirmed",Toast.LENGTH_SHORT).show();
                    Intent gotomoderate = new Intent(this, ModeratePosts.class);
                    startActivity(gotomoderate);
                }
                else{
                    Toast.makeText(getApplicationContext(),"You need to be a moderator to access this function", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.dSettings:{
                Toast.makeText(getApplicationContext(), "This feature will be implemented soon", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.dFeedback:{
                Toast.makeText(getApplicationContext(), "This feature will be implemented soon", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.dLogout:{
                SharedPreferences removeinfo = getSharedPreferences("logininfo", Context.MODE_PRIVATE);

                if(removeinfo.getString("username","").equals("")) {
                    Toast.makeText(getApplicationContext(), "You are not logged in", Toast.LENGTH_SHORT).show();
                    break;
                }

                else {
                    SharedPreferences.Editor editor = removeinfo.edit();

                    editor.putString("username", "");
                    editor.putString("password", "");
                    editor.apply();

                    Toast.makeText(getApplicationContext(), "You are now logged out", Toast.LENGTH_SHORT).show();

                    Intent backtofront = new Intent(PostsActivity.this, FirstTimeActivity.class);
                    startActivity(backtofront);
                    finish();
                    break;
                }
            }
        }

        mDrawerlayout.closeDrawer(GravityCompat.START);
        Log.d("NavItems","out");
        return true;
    }

    public class GetPosts extends AsyncTask<String, Void, List<Posts>>{

        @Override
        protected List<Posts> doInBackground(String... params) {
            URL url = null;
            try {
                url = new URL("http://campusdiaries.pythonanywhere.com/retrieveposts");
            } catch (Exception e) {
                Log.e("EXception", "URLS", e);
            }
            String searchResults = null;
            try {
                searchResults = getJsonData(url);
            } catch (Exception e) {
                Log.e("MYAPP", "Exception", e);
            }

            String jsonstring = searchResults;
            List<Posts> postsList = new ArrayList<>();

            try {
                JSONObject parentObject = new JSONObject(jsonstring);
                JSONArray parentArray = parentObject.getJSONArray("AllPosts");

                for (int i = 0; i < parentArray.length(); i++) {

                    JSONObject finalObject = parentArray.getJSONObject(i);
                    Posts post = new Posts();

                    post.setTitle(finalObject.getString("title"));
                    post.setClub(finalObject.getString("club"));
                    post.setContact(finalObject.getInt("contact"));
                    post.setEnddate(finalObject.getString("enddate"));
                    post.setPostedby(finalObject.getInt("postedby"));
                    post.setPostpic(finalObject.getString("postpic"));
                    post.setShortdesc(finalObject.getString("shortdesc"));
                    post.setStartdate(finalObject.getString("startdate"));

                    postsList.add(post);
                }
            }catch(Exception e)
            {
                Log.e("JSON","Exception",e);
            }

            return postsList;
        }

        @Override
        protected void onPostExecute(List<Posts> postsList) {
            super.onPostExecute(postsList);

            PostsAdapter adapter = new PostsAdapter(getApplicationContext(), R.layout.post, postsList);
            lvPosts.setAdapter(adapter);
        }
    }

    public static String getJsonData(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        Log.d("JSON","Getting JSON data");
        urlConnection.setUseCaches(false);

        try {
            Log.d("Response Code", Integer.toString(urlConnection.getResponseCode()));
            InputStream in = null;
            in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } catch(Exception e) {
            Log.e("HTTPError", "Exception", e);
        }
        finally {
            urlConnection.disconnect();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
        {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.action_refresh){
            new GetPosts().execute("Execute");
            return true;
        }

        if(mToggle.onOptionsItemSelected(item))
            return true;

         return super.onOptionsItemSelected(item);
    }

    public class PostsAdapter extends ArrayAdapter{

        private List<Posts> postsList;
        private int resource;
        private LayoutInflater inflater;

        public PostsAdapter(Context context, int resource, List<Posts> objects) {

            super(context, resource, objects);
            postsList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            ViewHolder holder = null;

            Log.d("PostsAdapter","inside get view");

            if (convertView == null){
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.ivPost = (ImageView)convertView.findViewById(R.id.posticon);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                //holder.postedby = (TextView)convertView.findViewById(R.id.postedby);
                holder.Club = (TextView)convertView.findViewById(R.id.Club);
                holder.startdate = (TextView)convertView.findViewById(R.id.startdate);
                //holder.enddate = (TextView)convertView.findViewById(R.id.enddate);
                holder.contact = (TextView)convertView.findViewById(R.id.contact);
                holder.Description = (TextView)convertView.findViewById(R.id.Description);

                convertView.setTag(holder);
            }else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            Log.d("POstsAdapter","Assigning views");

            final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

            ImageLoader.getInstance().displayImage(postsList.get(position).getPostpic(), holder.ivPost, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view){
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            Log.d("PostsAdapter","Setting values to views");

            holder.title.setText(postsList.get(position).getTitle());
            //holder.postedby.setText("Posted By: " + String.valueOf(postsList.get(position).getPostedby()));
            holder.Club.setText(postsList.get(position).getClub());
            holder.startdate.setText(postsList.get(position).getStartdate());
            //holder.enddate.setText("End date: " + postsList.get(position).getEnddate());
            holder.contact.setText(String.valueOf(postsList.get(position).getContact()));
            holder.Description.setText(postsList.get(position).getShortdesc());

            Log.d("POstsAdapter","Returning convertview");

            return convertView;
        }

        class ViewHolder{
            private ImageView ivPost;
            private TextView title;
            private TextView postedby;
            private TextView Club;
            private TextView startdate;
            private TextView enddate;
            private TextView contact;
            private TextView Description;
        }
    }
}
