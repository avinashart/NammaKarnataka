package smartAmigos.com.nammakarnataka;


import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import smartAmigos.com.nammakarnataka.adapter.DatabaseHelper;
import smartAmigos.com.nammakarnataka.adapter.generic_adapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class VisitedFragment extends Fragment {


    public VisitedFragment() {
        // Required empty public constructor
    }

    static SimpleDraweeView draweeView;

    private List<generic_adapter> visited_adapterList = new ArrayList<>();

    View view;
    Context context;
    ListView list;
    TextView t;
    DatabaseHelper myDBHelper;
    Cursor cursor, PlaceCursor;
    int id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_visited, container, false);
        context = getActivity().getApplicationContext();

        t = (TextView) view.findViewById(R.id.pi1);
        Typeface myFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/placenames.otf");
        t.setTypeface(myFont);

        list = (ListView) view.findViewById(R.id.visitedList);

        visited_adapterList.clear();
        Fresco.initialize(getActivity());


        myDBHelper = new DatabaseHelper(context);
        PlaceCursor = myDBHelper.getAllVisited();


        while(PlaceCursor.moveToNext()){
            id = PlaceCursor.getInt(0);


            String [] imagesArray = new String[25];
            Cursor imageURLCursor = myDBHelper.getAllImagesArrayByID(id);
            for (int i=0;imageURLCursor.moveToNext();i++){
                imagesArray[i] = imageURLCursor.getString(1);
            }


            cursor = myDBHelper.getPlaceById(id);

            while(cursor.moveToNext()){

                visited_adapterList.add(
                        new generic_adapter(
                                imagesArray,        //id
                                cursor.getString(1),//name
                                cursor.getString(2),//description
                                cursor.getString(3),//district
                                cursor.getString(4),//best season
                                cursor.getString(5),//additional info
                                cursor.getString(6),//nearby place
                                cursor.getDouble(7),//latitude
                                cursor.getDouble(8) //longitude
                        ));
            }

        }

        displayList();


        return view;
    }



    private void displayList() {
        final ArrayAdapter<generic_adapter> adapter = new myVisitedListAdapterClass();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaceCursor.moveToPosition(position);
                int img_id = PlaceCursor.getInt(0);

                Fragment fragment = new placeDisplayFragment(img_id);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_main, fragment);
                ft.addToBackStack(null);
                ft.commit();

            }
        });


        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {

                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle("Delete from my places ?\n");
                adb.setCancelable(false);
                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PlaceCursor.moveToPosition(pos);
                        int img_id = PlaceCursor.getInt(0);

                        myDBHelper = new DatabaseHelper(context);
                        myDBHelper.deleteFromVisited(img_id);

                        adapter.remove(adapter.getItem(pos));
                        adapter.notifyDataSetChanged();


                    } });
                adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    } });
                adb.show();

                return true;
            }
        });
    }



    public class myVisitedListAdapterClass extends ArrayAdapter<generic_adapter> {
        myVisitedListAdapterClass() {
            super(context, R.layout.item, visited_adapterList);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                itemView = inflater.inflate(R.layout.item, parent, false);

            }
            generic_adapter current = visited_adapterList.get(position);

            Uri uri = Uri.parse(current.getImage()[0]);
            draweeView = (SimpleDraweeView) itemView.findViewById(R.id.item_Image);
            draweeView.getHierarchy().setProgressBarImage(new CircleProgressBarDrawable(1));
            draweeView.setImageURI(uri);

            TextView t_name = (TextView) itemView.findViewById(R.id.item_Title);
            t_name.setText(current.getTitle());

            TextView t_dist = (TextView) itemView.findViewById(R.id.item_Dist);
            t_dist.setText(current.getDistrict());

            return itemView;
        }
    }



}
