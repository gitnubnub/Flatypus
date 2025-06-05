package si.uni_lj.fe.tnuv.flatypus.ui.settings;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import si.uni_lj.fe.tnuv.flatypus.R;

public class ProfilePictureAdapter extends BaseAdapter {

    private Context context;
    private int[] profilePictures;
    private int selectedPosition = -1; // Track the selected item

    public ProfilePictureAdapter(Context context, int[] profilePictures) {
        this.context = context;
        this.profilePictures = profilePictures;
    }

    @Override
    public int getCount() {
        return profilePictures.length;
    }

    @Override
    public Object getItem(int position) {
        return profilePictures[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_profile_picture, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.profile_picture_item);
        imageView.setImageResource(profilePictures[position]);

        // Highlight selected item
        if (position == selectedPosition) {
            imageView.setBackgroundResource(R.drawable.selected_profile_picture_background);
        } else {
            imageView.setBackgroundResource(0);
        }

        return convertView;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}