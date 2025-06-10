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
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;

public class ToDoFragment extends Fragment {

    private FragmentToDoBinding binding;
    private si.uni_lj.fe.tnuv.flatypus.ui.to_do.ToDoViewModel toDoViewModel;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        toDoViewModel = new ViewModelProvider(this).get(ToDoViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding = FragmentToDoBinding.inflate(inflater, container, false);

        toDoViewModel.initializeWithUserViewModel(userViewModel);
        View root = binding.getRoot();

        LinearLayout todoListContainer = binding.todoListContainer;
        toDoViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            todoListContainer.removeAllViews();
            for (int i = 0; i < tasks.size(); i++) {
                ToDoViewModel.Task task = tasks.get(i);
                View taskView = inflater.inflate(R.layout.task_layout, todoListContainer, false);

                CheckBox taskCheckbox = taskView.findViewById(R.id.task_checkbox);
                ImageView assigneeProfilePicture = taskView.findViewById(R.id.assignee_profile_picture);
                TextView taskName = taskView.findViewById(R.id.task_name);

                taskCheckbox.setChecked(task.isCompleted());
                taskName.setText(task.getName());

                // Dynamically fetch profile picture based on assignee email
                userViewModel.getUserByMail(task.getAssignee()).observe(getViewLifecycleOwner(), user -> {
                    if (user != null) {
                        assigneeProfilePicture.setImageResource(user.getProfilePicture());
                    } else {
                        assigneeProfilePicture.setImageResource(R.drawable.pfp_red); // Default if not found
                    }
                });

                final int position = i;
                taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    String currentUser = toDoViewModel.getCurrentUser();
                    if (!task.getAssignee().equals(currentUser) && !task.isCompleted()) {
                        showReassignWarningDialog(task, position, isChecked);
                        taskCheckbox.setChecked(task.isCompleted());
                    } else {
                        toDoViewModel.updateTaskCompletion(position, isChecked);
                    }
                });

                todoListContainer.addView(taskView);
            }
        });

        binding.addTaskButton.setOnClickListener(v -> showAddTaskDialog());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        toDoViewModel.fetchTasks();
    }

    private void showReassignWarningDialog(ToDoViewModel.Task task, int position, boolean isChecked) {
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
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_task_layout, null);

        EditText taskNameInput = dialogView.findViewById(R.id.task_name_input);
        Spinner assigneeSpinner = dialogView.findViewById(R.id.assignee_spinner);
        Spinner repeatSpinner = dialogView.findViewById(R.id.repeat_spinner);
        Button addTaskButton = dialogView.findViewById(R.id.add_task_confirm_button);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), currentUser -> {
            if (currentUser != null) {
                String currentApartment = currentUser.getCurrentApartment();
                if (currentApartment != null && !currentApartment.isEmpty()) {
                    userViewModel.getRoommates(currentApartment).observe(getViewLifecycleOwner(), roommates -> {
                        ArrayList<String> usernameOptions = new ArrayList<>();
                        ArrayList<String> emailOptions = new ArrayList<>();
                        usernameOptions.add("Random");
                        emailOptions.add("random");

                        if (roommates != null) {
                            for (UserViewModel.User user : roommates) {
                                usernameOptions.add(user.getUsername());
                                emailOptions.add(user.getEmail());
                            }
                        }

                        ArrayAdapter<String> assigneeAdapter = new ArrayAdapter<>(
                                requireContext(), android.R.layout.simple_spinner_item, usernameOptions);
                        assigneeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        assigneeSpinner.setAdapter(assigneeAdapter);

                        addTaskButton.setOnClickListener(v -> {
                            String taskName = taskNameInput.getText().toString().trim();
                            int selectedPosition = assigneeSpinner.getSelectedItemPosition();
                            String repeat = repeatSpinner.getSelectedItem().toString().toLowerCase();

                            if (!taskName.isEmpty()) {
                                String assigneeEmail = emailOptions.get(selectedPosition);
                                toDoViewModel.addTask(taskName, assigneeEmail, repeat);
                                ((AlertDialog) addTaskButton.getTag()).dismiss();
                            } else {
                                taskNameInput.setError("Task name cannot be empty");
                            }
                        });
                    });
                }
            }
        });

        String[] repeatOptions = {"None", "Weekly", "Monthly"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, repeatOptions);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(repeatAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Task")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();
        dialog.show();
        addTaskButton.setTag(dialog);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}