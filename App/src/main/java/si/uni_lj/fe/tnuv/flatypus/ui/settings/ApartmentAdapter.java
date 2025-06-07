package si.uni_lj.fe.tnuv.flatypus.ui.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.data.Apartment;

public class ApartmentAdapter extends BaseAdapter {

    private Context context;
    private List<Apartment> apartments;

    public ApartmentAdapter(Context context, List<Apartment> apartments) {
        this.context = context;
        this.apartments = apartments;
    }

    @Override
    public int getCount() {
        return apartments.size();
    }

    @Override
    public Object getItem(int position) {
        return apartments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_apartment, parent, false);
        }

        Apartment apartment = apartments.get(position);

        TextView nameTextView = convertView.findViewById(R.id.apartment_name);

        nameTextView.setText(apartment.getName());

        return convertView;
    }

    public void addApartment(Apartment apartment) {
        apartments.add(apartment);
        notifyDataSetChanged();
    }
}