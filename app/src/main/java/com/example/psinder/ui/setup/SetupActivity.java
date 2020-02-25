package com.example.psinder.ui.setup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.psinder.MainActivity;
import com.example.psinder.R;
import com.example.psinder.models.UserSetupModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    // Dzieki temu ze dodaje sobie tu zmienna ktora przechowuje odwolanie do bazy do Childa "Users"
    // nigdzie sie nie pierdolne po drodze i nie zaczne odwolywac sie do np "Usres"
    // i gdybym w razie w chcial zmienic cos w przyszlosci, to zmieniam to w jednym miejscu, nie w 10
    // Stringi gdzie wyswietlasz jakas informacje dla uzytkownika lepiej zapisywac sobie w strings.xml
    private static final String FIREBASE_USER_DATABASE_REFERENCE = "Users";
    private static final String FIREBASE_PROFILE_IMAGE_DATABASE_REFERENCE = "profileimage";
    private static final int PICK_IMAGE_FROM_GALLERY_REQUEST_CODE = 1;

    // w Javie jest tak ze zaczynamy mala litera, nastepne slowo z duzej, czy to zmienna czy to metoda
    // "UserName" raczej nic nam nie mowi, lepiej nazwac UserNameEditText, to samo do reszty zmiennych
    private EditText userNameEditText, fullNameEditText, countryNameEditText;
    private Button saveInformationButton;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;

    // Pogrupowalem na rzeczy ktore sa nie zmienne, rzeczy ktore dotycza widoku, i rzeczy ktore dotycza firebase, dzieki temu lepiej sie to czyta
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currenUserID;
    private StorageReference userProfileImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Teraz jest lepiej czytelne co sie dzieje w onCreate, a zmian dokonujesz juz w konkretnej metodce
        // Tylko wiadomo, przyciski itp musza byc zadeklarowane wczesniej niz opcje ich uzycia
        initFirebase();
        initViews();
        initOnClickListeners();
    }

    private void initOnClickListeners() {
        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, PICK_IMAGE_FROM_GALLERY_REQUEST_CODE);

            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild(FIREBASE_PROFILE_IMAGE_DATABASE_REFERENCE)) {
                        String image = dataSnapshot.child(FIREBASE_PROFILE_IMAGE_DATABASE_REFERENCE).getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                    } else {
                        showToastMessage("Please select profile image");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currenUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child(FIREBASE_USER_DATABASE_REFERENCE).child(currenUserID);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }

    private void initViews() {
        userNameEditText = (EditText) findViewById(R.id.setup_username);
        fullNameEditText = (EditText) findViewById(R.id.setup_full_name);
        countryNameEditText = (EditText) findViewById(R.id.setup_country_name);
        saveInformationButton = (Button) findViewById(R.id.setup_information_button);
        profileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_FROM_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        // Get the cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {       // store the cropped image into result
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                showLoadingBar("Profile Image", "Please wait, while we updating your profile image...");
                Uri resultUri = result.getUri();

                final StorageReference filePath = userProfileImageRef.child(currenUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                usersRef.child(FIREBASE_PROFILE_IMAGE_DATABASE_REFERENCE).setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dismissLoadingBar();
                                        if (task.isSuccessful()) {
                                            Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                            startActivity(selfIntent);
                                            showToastMessage("Image Stored");
                                        } else {
                                            String message = task.getException().getMessage();
                                            showToastMessage("Error:" + message);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            } else {
                showToastMessage("Error Occured: Image can not be cropped. Try Again.");
                dismissLoadingBar();
            }
        }
    }

    private void showLoadingBar(String title, String message) {
        // Nie duplikujemy kodu, najlepiej tworzyc takie metody odpowiedzialne tylko za jedna rzecz
        if (loadingBar == null) {
            loadingBar = new ProgressDialog(this);
        }
        loadingBar.setTitle(title);
        loadingBar.setMessage(message);
        loadingBar.setCanceledOnTouchOutside(false); // tu raczej widzialbym false (bylo true), blokuj uzytkownikowi takie opcje jesli cos robisz pod spodem, ale mimo wszystko, zalezy od Ciebie
        loadingBar.show();
    }

    private void dismissLoadingBar() {
        if (loadingBar != null) {
            loadingBar.dismiss();
        }
    }

    private void saveAccountSetupInformation() {
        String username = userNameEditText.getText().toString();
        String fullname = fullNameEditText.getText().toString();
        String country = countryNameEditText.getText().toString();

        if (TextUtils.isEmpty(username)) {
            showToastMessage("Please write your username");
            return; // Ten return zatrzyma wykonywanie metody dalej, inaczej jak ktos nie uzupelni wszystkich trzech pol to nam wyswietli 3 Toasty
        }
        if (TextUtils.isEmpty(fullname)) {
            showToastMessage("Please write your full name");
            return;
        }
        if (TextUtils.isEmpty(country)) {
            showToastMessage("Please write your country");
            return;
        }

        showLoadingBar("Saving Information","Please wait, while we are creating your account...");

        // Tu zmieniłem implementacje, wyjebalem hasha, za kazdym razem rob sobie model obiektu na jakim chcesz pracowac,
        // dopiero pozniej mozesz go w taki sposob ustawiac i wysylac tak jak ponizej
        // Robie to w ciemno, u siebie mam podobna implementacje tylko moglem pojebac childy, gdyby cos na bazie sie pojawilo dziwnego
        // to pewnie ja :P
        UserSetupModel userSetupModel = new UserSetupModel();
        userSetupModel.setUsername(username);
        userSetupModel.setFullname(fullname);
        userSetupModel.setCountry(country);
        userSetupModel.setStatus("Hey there");
        userSetupModel.setGender("none");
        userSetupModel.setDob("none");

        usersRef.push()
                .setValue(userSetupModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dismissLoadingBar();
                if (task.isSuccessful()) {
                    sendUserToMainActivity();
                    showToastMessage("your account is created successfully");
                } else {
                    String message = task.getException().getMessage();
                    if (message != null) {
                        showToastMessage("Error Occured: " + message);
                    }
                }
            }
        });
    }

    private void showToastMessage(String message) {
        // Toasta robimy w kilku miejscach, nie ma potrzeby duplikowania kodu, tu wyswietli nam to co mu podamy w wywolaniu
        Toast.makeText(SetupActivity.this, message, Toast.LENGTH_LONG).show();
    }


    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
