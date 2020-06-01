package com.dal.mc.servicegenie;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RViewAdapter extends RecyclerView.Adapter<RViewAdapter.ViewHolder> {

    ArrayList<Booking> bookingList;
    Context context;
    onClickListener listener;

    public RViewAdapter(Context context, ArrayList<Booking> bookingList,onClickListener listener)
    {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    //inflate the layout for the view of every item in the Recycler View
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        context = parent.getContext();
        return viewHolder;
    }

    @Override
    //Binding the data on views
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {


        if (bookingList.size() > 0) {
            Booking booking = bookingList.get(position);


            holder.serviceName.setText(booking.getRequestedServiceName());
            holder.serviceDateTime.setText(booking.getRequestTimeandDate());
            holder.serviceProfName.setText(booking.getRequestProfName());
            holder.serviceStatus.setText(booking.getRequestStatus());
            holder.serviceCost.setText(booking.getRequestCost());

            holder.helpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent = new Intent(context, activity_help.class);
                    context.startActivity(intent);*/
                    if(listener!=null)
                    {
                        listener.onItemClickListener(holder.getAdapterPosition(),bookingList.get(holder.getAdapterPosition()));
                    }
                }
            });
        }

    }


    @Override
    //number of recyclerview required
    public int getItemCount() {
        return bookingList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName,serviceDateTime,serviceProfName,serviceStatus,serviceCost;
        Button helpBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            serviceName = (TextView) itemView.findViewById(R.id.serviceName);
            serviceDateTime = (TextView) itemView.findViewById(R.id.serviceDateTime);
            serviceProfName = (TextView) itemView.findViewById(R.id.serviceProfName);
            serviceStatus = (TextView) itemView.findViewById(R.id.serviceStatus);
            serviceCost = (TextView) itemView.findViewById(R.id.serviceCost);
            helpBtn = itemView.findViewById(R.id.helpBtn);
            /*helpBtn = (Button) itemView.findViewById(R.id.helpBtn);

            helpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(, activity_help.class);
                    context.startActivity();
                }
            });*/

        }

    }

    public void doRefresh(ArrayList<Booking> bookings)
    {
        this.bookingList = bookings;
        notifyDataSetChanged();
    }

    public interface onClickListener
    {
        void onItemClickListener(int position,Booking booking);
    }


}
