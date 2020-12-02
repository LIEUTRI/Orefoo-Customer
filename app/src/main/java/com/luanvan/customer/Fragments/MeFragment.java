package com.luanvan.customer.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.luanvan.customer.LoginActivity;
import com.luanvan.customer.MainActivity;
import com.luanvan.customer.ManagerProfileActivity;
import com.luanvan.customer.PaymentActivity;
import com.luanvan.customer.R;
import com.luanvan.customer.SignupActivity;
import com.luanvan.customer.UpdateProfileActivity;
import com.luanvan.customer.components.RequestUrl;
import com.luanvan.customer.components.RequestsCode;
import com.luanvan.customer.components.ResultsCode;
import com.luanvan.customer.components.Shared;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MeFragment extends Fragment {
    private MaterialButton btnLogin, btnSignup;
    private TextView tvManagerProfile, tvLogout, tvSetting;
    private RelativeLayout layoutProgressBar;
    private ProgressBar progressBar;
    private LinearLayout layoutNotLogin, layoutLogin;
    private TextView tvUsername, tvName;
    private CircleImageView ivProfile, ivChangePhoto;
    private String token = "";
    private int consumerID = -1;
    private int userId;
    private String username = "";
    private String firstName = "";
    private String lastName = "";
    private String phoneNumber = "";
    private String dayOfBirth = "";
    private String gender = "";
    private String email = "";
    private String profileImage = "";

    private Intent intentChooser;
    private static String TEMP_IMAGE_NAME;
    private Uri uriProfile;
    private StorageReference storageRef;
    private StorageReference shipperImagesRef;

    public MeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnSignup = view.findViewById(R.id.btnSignup);
        tvManagerProfile = view.findViewById(R.id.tvManagerProfile);
        tvLogout = view.findViewById(R.id.tvLogout);
        tvSetting = view.findViewById(R.id.tvSetting);
        layoutProgressBar = view.findViewById(R.id.layoutProfile);
        layoutNotLogin = view.findViewById(R.id.layoutNotLogin);
        layoutLogin = view.findViewById(R.id.layoutLogin);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvName = view.findViewById(R.id.tvName);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivChangePhoto = view.findViewById(R.id.ivChangePhoto);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleSmall);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutProgressBar.addView(progressBar, params);
        progressBar.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Shared.KEY_BEARER, "")+"";
        if (!token.equals("")){
            // logged in
            userId = getUserId(token);
            username = getUsername(token);
            sharedPreferences = getActivity().getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);
            consumerID = sharedPreferences.getInt(Shared.KEY_CONSUMER_ID, -1);
            new GetConsumerDataTask().execute();
        } else {
            layoutNotLogin.setVisibility(View.VISIBLE);
        }
        /////////////////////////////////////////////////////////////////////////

        // set Profile image
        ivProfile.setImageResource(R.drawable.ic_person_24);
        ivProfile.setCircleBackgroundColorResource(R.color.extra_light_gray);

        ivChangePhoto.setImageResource(R.drawable.ic_edit_20);
        ivChangePhoto.setCircleBackgroundColorResource(R.color.colorPrimary);
        ivChangePhoto.setPadding(5,5,5,5);
        /////////////////////////////////////////////////////////////////////////
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        ///////////////////////////////////////////////////////////////


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SignupActivity.class));
            }
        });
        tvManagerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ManagerProfileActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("dayOfBirth", dayOfBirth);
                intent.putExtra("gender", gender);
                intent.putExtra("email", email);
                intent.putExtra("consumerID", consumerID);
                startActivity(intent);
            }
        });
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Shared.TOKEN, Context.MODE_PRIVATE).edit();
                editor.putString(Shared.KEY_BEARER, "");
                editor.apply();

                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.logout_success), Toast.LENGTH_LONG).show();

                layoutLogin.setVisibility(View.INVISIBLE);
                layoutNotLogin.setVisibility(View.VISIBLE);

                progressBar.setVisibility(View.INVISIBLE);

                Intent i = new Intent(getActivity(), LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });

        ivChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestsCode.REQUEST_PERMISSION_TO_PICK_PROFILE_IMAGE);
                    return;
                }

                TEMP_IMAGE_NAME = "consumer"+consumerID+"_profile_image.jpg"; // use to store picture when take picture by camera
                intentChooser = getPickImageIntent(getActivity());
                startActivityForResult(intentChooser, RequestsCode.REQUEST_PICK_PROFILE_IMAGE);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    public int getUserId(String token){
        String TOKEN_PREFIX = "Bearer ";
        JWT jwt = new JWT(token.replace(TOKEN_PREFIX,""));
        Claim claim = jwt.getClaim("userId");
        return claim.asInt();
    }
    public String getUsername(String token){
        String TOKEN_PREFIX = "Bearer ";
        JWT jwt = new JWT(token.replace(TOKEN_PREFIX,""));
        return jwt.getSubject();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetConsumerDataTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(RequestUrl.CONSUMER +"user/"+userId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", token);
                connection.connect();

                InputStream is = null;
                int statusCode = connection.getResponseCode();
                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }

                reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("ResponseGetUserData: ", "> " + line);
                }
                return buffer.toString();
            } catch (SocketTimeoutException e) {
                Toast.makeText(getActivity(), getResources().getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.INVISIBLE);
            layoutLogin.setVisibility(View.VISIBLE);

            if (result == null) return;
            try {
                JSONObject consumer = new JSONObject(result);
                firstName = consumer.getString("firstName").equals("null") ? "empty":consumer.getString("firstName");
                lastName = consumer.getString("lastName").equals("null") ? "empty":consumer.getString("lastName");
                phoneNumber = consumer.getString("phoneNumber").equals("null") ? "empty":consumer.getString("phoneNumber");
                dayOfBirth = consumer.getString("dayOfBirth").equals("null") ? "empty":consumer.getString("dayOfBirth");
                gender = consumer.getString("gender").equals("null") ? "empty":consumer.getString("gender");
                email = consumer.getString("email").equals("null") ? "empty":consumer.getString("email");
                profileImage = consumer.getString("profileImage");

                tvUsername.setText(username);
                tvName.setText(firstName.equals("empty")&&lastName.equals("empty") ? "(empty)":lastName+" "+firstName);

                if (profileImage.length() > 1){
                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.ic_person_24)
                            .error(R.drawable.ic_person_24);
                    Glide.with(getActivity()).load(profileImage).apply(options).into(ivProfile);
                }

                ivChangePhoto.setImageResource(R.drawable.ic_edit_20);
                ivChangePhoto.setCircleBackgroundColorResource(R.color.colorPrimary);
                ivChangePhoto.setPadding(5,5,5,5);

                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE).edit();
                editor.putString(Shared.KEY_FIRST_NAME, firstName);
                editor.putString(Shared.KEY_LAST_NAME, lastName);
                editor.putString(Shared.KEY_USERNAME, username);
                editor.putString(Shared.KEY_PHONE, phoneNumber);
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    ///////////////////////////////////
    // choose image picker
    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(TEMP_IMAGE_NAME)));
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }
    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    private static File getTempFile(String name) {
        File imageFile = new File(Environment.getExternalStorageDirectory()+"/Android/media/com.luanvan.customer/images", name);
        try {
            imageFile.getParentFile().mkdirs();
        }catch (Exception e){
            Log.i("MeFragment", "getTempFile: " + e.getMessage());
        }

        return imageFile;
    }

    public static Uri getImageUriFromResult(Context context, int resultCode, Intent imageReturnedIntent) {
        Uri selectedImage = null;
        File imageFile = getTempFile(TEMP_IMAGE_NAME);
        if (resultCode == Activity.RESULT_OK) {

            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null  ||
                    imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {     /** CAMERA **/
                selectedImage = Uri.fromFile(imageFile);
                Log.i("ManagerProfileActivity", "uri: "+selectedImage.getPath());
            } else {            /** GALLERY **/
                selectedImage = imageReturnedIntent.getData();
                Log.i("MeFragment", "uri: "+selectedImage.getPath());
                Log.i("MeFragment", "path: "+getRealPathFromUri(context, selectedImage));
            }
        }
        return selectedImage;
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void uploadImage(final Uri uriImage, final String newName){
        if (uriImage == null) return;

        // location to save images in Firebase
        shipperImagesRef = storageRef.child("images/"+newName);

        UploadTask uploadTask = shipperImagesRef.putFile(uriImage);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(), "image uploaded", Toast.LENGTH_SHORT).show();
                getUrl(newName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Could not uploaded. Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getUrl (final String fileName){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("images");
        StorageReference dateRef = storageRef.child(fileName);
        dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl) {
                Log.i("MeFragment", "imageUrl: "+downloadUrl.toString());

                profileImage = downloadUrl.toString();

                new UpdateProfileImageTask().execute(consumerID+"", profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("MeFragment", e.getMessage());
            }
        });
    }
    //////////////////////////////////
    @SuppressLint("StaticFieldLeak")
    private class UpdateProfileImageTask extends AsyncTask<String,String,String> {
        private OutputStream os;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            //http post
            try {
                URL url = new URL(RequestUrl.CONSUMER + strings[0] + "/update-profile-image");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("profileImage", strings[1]);
                String data = jsonObject.toString();
                Log.i("json request", data);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "application/json;charset=utf-8");
                connection.setRequestProperty("Authorization", token);
                connection.setDoOutput(true);
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.connect();

                os = new BufferedOutputStream(connection.getOutputStream());
                os.write(data.getBytes());
                os.flush();

                InputStream is;
                int statusCode = connection.getResponseCode();
                Log.i("statusCode", statusCode+" | request: "+url.toString());
                if (statusCode == 200) return "200";
                if (statusCode >= 200 && statusCode < 400){
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                    Log.d("ResponseProfileImage: ", "> " + line);
                }
                return buffer.toString();

            } catch (SocketTimeoutException e) {
                return ResultsCode.SOCKET_TIMEOUT+"";
            } catch (IOException | JSONException e){
                e.printStackTrace();
            } finally {
                try {
                    if (os!=null) os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (connection != null) connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressBar.setVisibility(View.INVISIBLE);

            if (s == null) return;

            if (s.equals("200")){
                Log.v("MeFragment", "success update profile image");
            } else if (s.equals(ResultsCode.SOCKET_TIMEOUT+"")){
                Toast.makeText(getActivity(), getString(R.string.socket_timeout), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "error", Toast.LENGTH_LONG).show();
            }
        }
    }
    //////////////////////////////////

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestsCode.REQUEST_PICK_PROFILE_IMAGE && resultCode == Activity.RESULT_OK){

                uriProfile = getImageUriFromResult(getActivity(), resultCode, data);
                if (!uriProfile.getPath().contains(getActivity().getPackageName()))
                    uriProfile = Uri.fromFile(new File(getRealPathFromUri(getActivity(), uriProfile)));

                Log.i("MeFragment", "-->profile: "+uriProfile.getPath());

                ivProfile.setImageDrawable(null);
                ivProfile.setImageURI(uriProfile);

                uploadImage(uriProfile, TEMP_IMAGE_NAME);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestsCode.REQUEST_PERMISSION_TO_PICK_PROFILE_IMAGE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED){

            TEMP_IMAGE_NAME = "consumer"+consumerID+"_profile_image.jpg"; // use to store picture when take picture by camera
            intentChooser = getPickImageIntent(getActivity());
            startActivityForResult(intentChooser, RequestsCode.REQUEST_PICK_PROFILE_IMAGE);
        }
    }

    /////////////////////////////////
    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        if (getActivity()==null || tvName == null) return;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Shared.CONSUMER, Context.MODE_PRIVATE);
        firstName = sharedPreferences.getString(Shared.KEY_FIRST_NAME, "");
        lastName = sharedPreferences.getString(Shared.KEY_LAST_NAME, "");
        tvName.setText(lastName +" "+ firstName);

        super.onResume();
    }
}