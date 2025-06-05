package si.uni_lj.fe.tnuv.flatypus.ui.to_do;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentToDoBinding;


public class ToDoFragment extends Fragment {

    private FragmentToDoBinding binding;
    private si.uni_lj.fe.tnuv.flatypus.ui.todo.ToDoViewModel toDoViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        toDoViewModel = new ViewModelProvider(this).get(si.uni_lj.fe.tnuv.flatypus.ui.todo.ToDoViewModel.class);

        binding = FragmentToDoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // To-Do List
        LinearLayout todoListContainer = binding.todoListContainer;
        toDoViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            todoListContainer.removeAllViews();
            for (int i = 0; i < tasks.size(); i++) {
                si.uni_lj.fe.tnuv.flatypus.ui.todo.ToDoViewModel.Task task = tasks.get(i);
                View taskView = inflater.inflate(R.layout.task_layout, todoListContainer, false);

                // Set up task item
                CheckBox taskCheckbox = taskView.findViewById(R.id.task_checkbox);
                ImageView assigneeProfilePicture = taskView.findViewById(R.id.assignee_profile_picture);
                TextView taskName = taskView.findViewById(R.id.task_name);

                taskCheckbox.setChecked(task.isCompleted());
                taskName.setText(task.getName());

                // Set profile picture (replace with actual resource handling)
                if ("platypus".equals(task.getProfilePicture())) {
                    assigneeProfilePicture.setImageResource(R.drawable.platypus);
                } else if ("red_fluffy".equals(task.getProfilePicture())) {
                    assigneeProfilePicture.setImageResource(R.drawable.platypus);
                }

                // Handle checkbox interaction
                final int position = i;
                taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    String currentUser = toDoViewModel.getCurrentUser();
                    if (!task.getAssignee().equals(currentUser) && !task.isCompleted()) {
                        showReassignWarningDialog(task, position, isChecked);
                        taskCheckbox.setChecked(false); // Reset checkbox until confirmed
                    } else {
                        toDoViewModel.updateTaskCompletion(position, isChecked);
                    }
                });

                todoListContainer.addView(taskView);
            }
        });

        // Add Task Button
        binding.addTaskButton.setOnClickListener(v -> showAddTaskDialog());

        return root;
    }

    private void showReassignWarningDialog(si.uni_lj.fe.tnuv.flatypus.ui.todo.ToDoViewModel.Task task, int position, boolean isChecked) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Warning")
                .setMessage("This task is assigned to " + task.getAssignee() + ". Are you sure you want to take it?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    toDoViewModel.updateTaskCompletion(position, isChecked);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAddTaskDialog() {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_task_layout, null);

        EditText taskNameInput = dialogView.findViewById(R.id.task_name_input);
        Spinner assigneeSpinner = dialogView.findViewById(R.id.assignee_spinner);
        Spinner repeatSpinner = dialogView.findViewById(R.id.repeat_spinner);
        Button addTaskButton = dialogView.findViewById(R.id.add_task_confirm_button);

        // Populate assignee spinner
        List<String> assigneeOptions = new ArrayList<>(toDoViewModel.getRoommates());
        assigneeOptions.add("Random");
        ArrayAdapter<String> assigneeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, assigneeOptions);
        assigneeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assigneeSpinner.setAdapter(assigneeAdapter);

        // Populate repeat spinner
        String[] repeatOptions = {"None", "Weekly", "Monthly"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, repeatOptions);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(repeatAdapter);

        // Show dialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Task")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();
        dialog.show();

        // Handle add task button
        addTaskButton.setOnClickListener(v -> {
            String taskName = taskNameInput.getText().toString().trim();
            String assignee = assigneeSpinner.getSelectedItem().toString();
            String repeat = repeatSpinner.getSelectedItem().toString().toLowerCase();

            if (!taskName.isEmpty()) {
                toDoViewModel.addTask(taskName, assignee, repeat);
                dialog.dismiss();
            } else {
                taskNameInput.setError("Task name cannot be empty");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}