package fi.teemutt.hydroid.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.database.MyDataBaseHelper;
import fi.teemutt.hydroid.models.DrinkEvent;

public class DetailsFragment extends Fragment {

    private DetailsFragmentListener mCallback;
    private MyDataBaseHelper db;
    private MyAdapter adapter;
    private ArrayList<DrinkEvent> events;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_details, container, false);

        db = MyDataBaseHelper.getInstance(getContext());
        events = new ArrayList<>();

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(manager);
        rv.setHasFixedSize(true);

        adapter = new MyAdapter();
        rv.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        events = db.getEventsForDay(ZonedDateTime.now().with(LocalTime.of(0, 0)));
        adapter.notifyDataSetChanged();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drinkevent_list_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tvAmount.setText(String.format("%d ml", events.get(position).getSize()));
            holder.tvTime.setText(events.get(position).getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final TextView tvAmount;
            final TextView tvTime;
            final ImageButton btnEdit;
            final ImageButton btnTrash;

            MyViewHolder(View itemView) {
                super(itemView);
                tvAmount = (TextView) itemView.findViewById(R.id.tvAmount);
                tvTime = (TextView) itemView.findViewById(R.id.tvTime);
                btnEdit = (ImageButton) itemView.findViewById(R.id.btnEdit);
                btnTrash = (ImageButton) itemView.findViewById(R.id.btnTrash);
                btnTrash.setOnClickListener(this);
                btnEdit.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (view.getId() == btnEdit.getId()) {
                    Intent intent = new Intent(getContext(), EventEditActivity.class);
                    long _id = events.get(getAdapterPosition()).getId();
                    intent.putExtra("event_id", _id);
                    startActivity(intent);
                } else if (view.getId() == btnTrash.getId()) {
                    db.deleteEvent(events.get(getAdapterPosition()).getId());
                    events.remove(getAdapterPosition());
                    adapter.notifyItemRemoved(getAdapterPosition());
                    mCallback.onRowDeleted();
                }
            }
        }
    }

    // Interface to communicate with MainActivity.
    public interface DetailsFragmentListener {
        void onRowDeleted();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (DetailsFragmentListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnRowDeleted");
        }
    }
}
