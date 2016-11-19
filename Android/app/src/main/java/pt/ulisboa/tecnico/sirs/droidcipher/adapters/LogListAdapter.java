package pt.ulisboa.tecnico.sirs.droidcipher.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.R;
import pt.ulisboa.tecnico.sirs.droidcipher.data.Event;

/**
 * Created by goncalo on 18-11-2016.
 */

public class LogListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Event> data;
    private static LayoutInflater inflater = null;

    public LogListAdapter(Context context, ArrayList<Event> data) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(data.size()-position-1);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.log_list_row, null);
        TextView desc = (TextView) vi.findViewById(R.id.log_list_row_desc_tv);
        TextView date = (TextView) vi.findViewById(R.id.log_list_row_time_tv);
        ImageView img = (ImageView) vi.findViewById(R.id.log_list_row_img);
        Event event = data.get(data.size()-position-1);
        desc.setText(event.getDescription());
        CharSequence s = DateUtils.getRelativeTimeSpanString(event.getEventDate().getTime(), new Date().getTime(), 0L, DateUtils.FORMAT_ABBREV_ALL);
        date.setText(s.toString());
        if (event.getIcon() != -1) {
            switch (event.getIcon()) {
                case Constants.ICON_GOOD:
                    img.setImageResource(R.drawable.ic_arrows_circle_check);
                    break;
                case Constants.ICON_DENY:
                    img.setImageResource(R.drawable.ic_arrows_deny);
                    break;
                case Constants.ICON_LIGHTNING:
                    img.setImageResource(R.drawable.ic_arrows_circle_lightning);
                    break;
                default:
                    img.setImageResource(R.drawable.ic_arrows_circle_lightning);
                    break;
            }

        }
        return vi;
    }
}
