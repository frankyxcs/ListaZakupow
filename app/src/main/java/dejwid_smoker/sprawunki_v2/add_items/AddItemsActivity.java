package dejwid_smoker.sprawunki_v2.add_items;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import dejwid_smoker.sprawunki_v2.R;
import dejwid_smoker.sprawunki_v2.database.ListDatabaseHelper;
import dejwid_smoker.sprawunki_v2.fragments_main.AddListFragment;

public class AddItemsActivity extends AppCompatActivity
        implements AddItemFragment.OnListCategoryClicked ,
                    CategoryFragment.OnItemCategoryClicked,
                    AddItemFragment.OnConfirmButtonClicked {

    public static final String REST_OF_TABLE_NAME = "_table";
    public static final String VISIBLE_FRAGMENT = "visible_fragment";
    private static final String CURRENT_FRAGMENT = "current_fragment";
    private static final int SHOW_ITEMS_FRAGMENT = 0;
    private static final int ADD_ITEM_FRAGMENT = 1;
//    private static final int CATEGORY_FRAGMENT = 2;
    private static final boolean HOME_BUTTON_ENABLE = true;
    private static final boolean HOME_BUTTON_DISABLE = false;

    private FloatingActionButton fabAdd;
    private SQLiteDatabase db;
    private SQLiteOpenHelper openHelper;
    private Toolbar toolbar;
    private String listName;
    private int currentFrag;
    private boolean newList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_items);

        if (savedInstanceState != null) {
            currentFrag = savedInstanceState.getInt(CURRENT_FRAGMENT);
        } else {
            currentFrag = SHOW_ITEMS_FRAGMENT;
        }

        openHelper = new ListDatabaseHelper(this);
        listName = getIntent().getExtras().get(AddListFragment.LIST_NAME).toString();
        newList = (boolean) getIntent().getExtras().get(AddListFragment.NEW_LIST_NAME);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (newList) {
            addNewRecord(listName);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentByTag(VISIBLE_FRAGMENT);
                if (fragment instanceof ShowItemsFragment) {
                    currentFrag = SHOW_ITEMS_FRAGMENT;
                    fabAdd.show();
                    setToolbarToEachFrag(listName, HOME_BUTTON_ENABLE);
                }
                if (fragment instanceof AddItemFragment) {
                    currentFrag = ADD_ITEM_FRAGMENT;
                    fabAdd.hide();
                    setToolbarToEachFrag(getString(R.string.title_add_item), HOME_BUTTON_DISABLE);
                }
                if (fragment instanceof CategoryFragment) {
                    currentFrag = ADD_ITEM_FRAGMENT;
                    fabAdd.hide();
                    setToolbarToEachFrag("Kategoria", HOME_BUTTON_DISABLE);
                }
            }
        });

        showCurrFrag(currentFrag);
        createFabs();
    }

    private void setToolbarToEachFrag(String title, boolean homeEnabled) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(homeEnabled);
    }

    private void showCurrFrag(int cFrag) {
        Fragment fragment;
        switch (cFrag) {
            case SHOW_ITEMS_FRAGMENT:
                fragment = new ShowItemsFragment();
                showListItems(fragment);
                break;
            case ADD_ITEM_FRAGMENT:
                fragment = new AddItemFragment();
                runFragment(fragment);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_FRAGMENT, currentFrag);
    }

    private void createFabs() {
        fabAdd = (FloatingActionButton) findViewById(R.id.fab_test);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new AddItemFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_add_item, fragment, AddItemsActivity.VISIBLE_FRAGMENT)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onCategoryListClick(int position) {
        Fragment fragment = new CategoryFragment();

        Bundle args = new Bundle();
        args.putInt(CategoryFragment.POSITION_CATEGORY, position);
        fragment.setArguments(args);

        runFragment(fragment);
    }

    @Override
    public void oItemCategoryListClick(String recivedFromCateg) {
        addNewItemToDb(recivedFromCateg);
        showCurrFrag(SHOW_ITEMS_FRAGMENT);
    }

    @Override
    public void onConfirmButtonClicked(String recivedFromEditTxt) {
        addNewItemToDb(recivedFromEditTxt);
        showCurrFrag(SHOW_ITEMS_FRAGMENT);
    }

    private void addNewItemToDb(String justAdded) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("ITEM_NAME", justAdded);
            contentValues.put("ITEM_CHECKED", 0);

            db.insert(listName + REST_OF_TABLE_NAME, null, contentValues);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //odczyt i pokazanie itemow listy w showfrag
    private void showListItems(Fragment fragment) {

        try {
            db = openHelper.getReadableDatabase();
            Cursor cursor = db.query(listName + REST_OF_TABLE_NAME,
                    new String[] {"ITEM_NAME"},
                    null, null, null, null, null); //lub ITEM_CHECKED ASC / ITEM_CHECKED DESC

            int count = cursor.getCount();
            Bundle args = new Bundle();

            ArrayList<String> items = new ArrayList<>(count);
            if (count > 0) {
                int listNr = 0;

                if (cursor.moveToFirst()) {
                    //lists[listNr] = cursor.getString(0);
                    items.add(listNr, cursor.getString(0));
                    listNr++;
                }
                if (count > 1) {
                    while (cursor.moveToNext()) {
                        //lists[listNr] = cursor.getString(0);
                        items.add(listNr, cursor.getString(0));
                        listNr++;
                    }
                }
            }

            args.putStringArrayList(ShowItemsFragment.ITEMS_ARRAY, items);
            args.putString(ShowItemsFragment.CURRENT_NAME_LIST, listName);
            fragment.setArguments(args);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        runFragment(fragment);
    }

    //dodanie nowej listy po dialogu
    private void addNewRecord(String lName) {
        ContentValues contentValues = new ContentValues();
        try {
            db = openHelper.getWritableDatabase();

            db.execSQL("CREATE TABLE " + lName + REST_OF_TABLE_NAME
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "ITEM_NAME TEXT, "
                    + "ITEM_CHECKED INTEGER, "
                    + "ITEM_PRICE REAL, "
                    + "ITEM_COUNT REAL, "
                    + "ITEM_UNIT INTEGER);");

            contentValues.put("NAME", lName);
            db.insert("lists", null, contentValues);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //pobieraie listy z DB dla oItemCategoryListClick() i showListItems()
/*    private String getItemsFromDb() {
        String items = "";
        try {
            SQLiteOpenHelper openHelper = new ListDatabaseHelper(this);

            db = openHelper.getReadableDatabase();
            cursor = db.query("lists",
                    new String[] {"NAME", "ITEMS"},
                    "NAME = ?",
                    new String[] {listName},
                    null, null, null);

            if (cursor.moveToFirst()) {
                items =  cursor.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }*/

    private void runFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_add_item, fragment, VISIBLE_FRAGMENT)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}

