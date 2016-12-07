package fi.teemutt.hydroid.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

import fi.teemutt.hydroid.R;

/**
 * Created by Teemu on 18.11.2016.
 *
 */

public class SeekBarDialog extends DialogFragment {

    public interface SeekBarDialogListener {
        void onPositiveClick(int progress);
    }

    private SeekBarDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (SeekBarDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement SeekBarDialogListener");
        }
    }

    public static SeekBarDialog newInstance(int progress) {
        SeekBarDialog fragment = new SeekBarDialog();
        Bundle args = new Bundle();
        args.putInt("progress", progress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.seekbar_dialog_layout, null);

        Bundle args = getArguments();
        int progress = args.getInt("progress");

        final TextView tvValue = (TextView) v.findViewById(R.id.tvValue);
        tvValue.setText(String.format(Locale.getDefault(), "%d ml", progress * 10 + 1000));

        final SeekBar sb = (SeekBar) v.findViewById(R.id.seekBar);
        sb.setProgress(progress);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvValue.setText(String.format(Locale.getDefault(), "%d ml", seekBar.getProgress() * 10 + 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setView(v)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onPositiveClick(sb.getProgress());
                    }
                })
                .setTitle("Set daily goal manually");
        return builder.create();
    }
}
