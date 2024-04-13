package com.example.du_an1_qldt.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.du_an1_qldt.DAO.OrderDAO;
import com.example.du_an1_qldt.DAO.SanPhamDAO;
import com.example.du_an1_qldt.DAO.TaiKhoanDAO;
import com.example.du_an1_qldt.R;
import com.example.du_an1_qldt.model.Order;
import com.example.du_an1_qldt.model.OrderDetail;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    ArrayList<Order> orders;
    OrderDAO orderDAO;
    Context context;
    TaiKhoanDAO taiKhoanDAO;


    public OrderAdapter(Context context, ArrayList<Order> orders) {
        this.context = context;
        this.orders = orders;
        orderDAO = new OrderDAO((context));
    }

    @NonNull
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View v = inflater.inflate(R.layout.item_order, parent, false);
        OrderAdapter.ViewHolder holder = new OrderAdapter.ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.id.setText(String.valueOf(order.getId()));
        taiKhoanDAO = new TaiKhoanDAO(context);
        holder.nameCustomer.setText(taiKhoanDAO.getUserNameById(order.getIdUser()));
        holder.date.setText(order.getDateOrder());
       switch (order.getStatusOrder()){
           case 0:
               holder.status.setText("Chờ xác nhận");
               break;
           case 1:
               holder.status.setText("Đã xác nhận");
               break;
           case 2:
               holder.status.setText("Đã hủy");
               break;
       }
        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order.setStatusOrder(2);
                orderDAO.updateOrder(order);
                notifyDataSetChanged();
            }
        });
      if (order.getStatusOrder()==0){
          holder.btnConfirm.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  order.setStatusOrder(1);
                  holder.status.setText(String.valueOf(order.getStatusOrder()));
                  orderDAO.updateOrder(order);
                  updateProductQuantities(order.getId());
                  holder.layoutContainer.setVisibility(View.GONE);
                  notifyDataSetChanged();

              }
          });
      } else {
          // Nếu đơn hàng đã xác nhận, ẩn LinearLayout

          holder.layoutContainer.setVisibility(View.GONE);
      }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView id, nameCustomer, date, status, btnConfirm, btnCancel;
        LinearLayout layoutContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.idOrder);
            nameCustomer = itemView.findViewById(R.id.nameCustomer);
            date = itemView.findViewById(R.id.DateOrder);
            status = itemView.findViewById(R.id.statusOrder);
            btnConfirm = itemView.findViewById(R.id.btnConfirmOrder);
            btnCancel = itemView.findViewById(R.id.btnCancelOrder);
            layoutContainer=itemView.findViewById(R.id.linearLayout);
        }
    }
    private void updateProductQuantities(int orderId) {
        SanPhamDAO sanPhamDAO= new SanPhamDAO(context);
        // Lấy danh sách sản phẩm trong đơn hàng từ cơ sở dữ liệu
        ArrayList<OrderDetail> orderDetails = orderDAO.getlistOrderDetail(orderId);

        for (OrderDetail orderDetail : orderDetails) {
            // Lấy số lượng sản phẩm hiện tại từ cơ sở dữ liệu
            int currentQuantity = sanPhamDAO.getProductQuantityFromDatabase(orderDetail.getIdProduct());

            // Tính toán số lượng mới (ví dụ: giảm số lượng bằng số lượng trong đơn hàng)
            int updatedQuantity = currentQuantity - orderDetail.getQuantity();

            // Cập nhật số lượng sản phẩm mới vào cơ sở dữ liệu
            sanPhamDAO.updateProductQuantityInDatabase(orderDetail.getIdProduct(), updatedQuantity);
        }
    }
}
