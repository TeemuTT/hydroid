package fi.teemutt.hydroid.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

/**
 * Created by Teemu on 20.11.2016.
 *
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private TimePickerFragmentListener mListener;

    public interface TimePickerFragmentListener {
        void timeSet(int hour, int minute);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LocalTime time = LocalTime.now(ZoneId.systemDefault());
        int hour = time.getHour();
        int minute = time.getMinute();

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (TimePickerFragmentListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement TimePickerFragmentListener");
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        mListener.timeSet(i, i1);
    }
}