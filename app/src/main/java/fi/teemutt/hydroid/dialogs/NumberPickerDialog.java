package fi.teemutt.hydroid.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import fi.teemutt.hydroid.R;


/**
 * Created by Teemu on 18.11.2016.
 *
 */

public class NumberPickerDialog extends DialogFragment {

    private static final int DIALOG_WEIGHT = 1;
    private static final int DIALOG_AGE = 2;

    public interface NumberPickerListener {
        void onPositiveClick(int type, int value);
    }

    private NumberPickerListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (NumberPickerDialog.NumberPickerListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement NumberPickerListener");
        }
    }

    public static NumberPickerDialog newInstance(int type, int value) {
        NumberPickerDialog frag = new NumberPickerDialog();
        Bundle args = new Bundle();
        args.putInt("type", type);
        args.putInt("value", value);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Bundle args = getArguments();
        final int type = args.getInt("type");
        final int value = args.getInt("value");

        View v = inflater.inflate(R.layout.number_picker_dialog_layout, null);
        final NumberPicker picker = (NumberPicker) v.findViewById(R.id.numberPicker);
        String title = "Title";

        switch (type) {
            case DIALOG_WEIGHT:
                picker.setMinValue(40);
                picker.setMaxValue(160);
                picker.setValue(value);
                title = "Enter your weight";
                break;
            case DIALOG_AGE:
                picker.setMinValue(15);
                picker.setMaxValue(100);
                picker.setValue(value);
                title = "Enter your age";
                break;
        }

        builder.setView(v)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onPositiveClick(type, picker.getValue());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing.
                    }
                })
                .setTitle(title);

        return builder.create();
    }
}
