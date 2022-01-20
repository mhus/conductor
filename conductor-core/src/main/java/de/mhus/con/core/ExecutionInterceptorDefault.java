/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.con.core;

import java.util.LinkedList;
import java.util.List;

import de.mhus.con.api.ConUtil;
import de.mhus.con.api.Conductor;
import de.mhus.con.api.Context;
import de.mhus.con.api.ErrorInfo;
import de.mhus.con.api.ExecutionInterceptorPlugin;
import de.mhus.con.api.Project;
import de.mhus.con.api.Project.STATUS;
import de.mhus.con.api.Step;
import de.mhus.con.api.Steps;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MStopWatch;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.Console.COLOR;

public class ExecutionInterceptorDefault extends MLog implements ExecutionInterceptorPlugin {

    private LinkedList<Result> results;
    private MStopWatch watch;
    private LinkedList<Step> subSteps = new LinkedList<>();

    @Override
    public void executeBegin(Context context) {
        String step = context.getStep().getTitle();
        Console console = ConUtil.getConsole();
        console.flush();
        console.println();
        if (context.getCallLevel() == 0) {
            console.setBold(true);
            console.println("------------------------------------------------------------------------");
            console.setBold(false);
        }
        console.print("[");
        console.print(context.getExecutor().getCurrentStepCount());
        console.print("/");
        console.print(context.getExecutor().getLifecycle().getSteps().size());
        console.print("] ");
        if (context.getProject() != null && context.getProjects() != null) {
            console.print("[");
            int pIndex = 0;
            for (Project p : context.getProjects()) {
                pIndex++;
                if (p.getName().equals(context.getProject().getName())) break;
            }
            console.print(pIndex);
            console.print("/");
            console.print(context.getProjects().size());
            console.print("] ");

        } else {
            console.print(" --- ");
        }
        console.setColor(COLOR.RED, null);
        console.print(step);
        if (context.getProject() != null) {
            console.setColor(COLOR.BRIGHT_BLACK, null);
            console.print(" >>> ");
            console.setColor(COLOR.GREEN, null);
            console.print(context.getProject().getName());
        }
        console.println();
        if (context.getCallLevel() == 0) {
            console.setBold(true);
            console.println("------------------------------------------------------------------------");
            console.println();
            console.setBold(false);
            console.flush();
        }
    }

    @Override
    public void executeError(Context context, Throwable t) {
        results.add(new Result(STATUS.FAILURE, context, subSteps));
    }

    @Override
    public void executeEnd(Context context, boolean done) {
        if (done) 
            results.add(new Result(STATUS.SUCCESS, context, subSteps));
        else 
            results.add(new Result(STATUS.SKIPPED, context, subSteps));
    }

    @Override
    public void executeEnd(Conductor con, String lifecycle, Steps steps, List<ErrorInfo> errors) {
        watch.stop();
        boolean isError = false;
        Console console = ConUtil.getConsole();
        console.setBold(true);
        console.println("------------------------------------------------------------------------");
        console.print("  ");
        console.println(lifecycle);
        console.println();
        console.setBold(false);
        /* Per Step view */
        for (Step step : steps) {
            String name = step.getTitle() + " ";
            console.print("  ");
            console.print(name);
            console.print(MString.rep('.', 60 - name.length()));
            console.print(" ");
            STATUS status = getStepStatus(step);
            switch (status) {
                case FAILURE:
                    console.setColor(COLOR.BRIGHT_RED, null);
                    isError = true;
                    break;
                case SKIPPED:
                    console.setColor(COLOR.BRIGHT_YELLOW, null);
                    break;
                case SUCCESS:
                    console.setColor(COLOR.BRIGHT_GREEN, null);
                    break;
                default:
                    break;
            }
            console.println(status);
            console.cleanup();

            LinkedList<Result> subSteps = new LinkedList<>();
            
            for (Result result : results) {
                if (result.parent != null) {
                    if (con.isVerboseOutput()) {
                        if (result.main.getId() == step.getId()) {
                        	subSteps.addFirst(result);
                        }
                    }
                } else {
                    if (result.step.getId() == step.getId() && result.project != null) {
                        Project p = result.project;
                        String pn = p.getName();
                        if (p.getStatus() == STATUS.SKIPPED) 
                            console.setColor(COLOR.BRIGHT_BLACK, null);
                        else 
                            console.setColor(COLOR.WHITE, null);
                        console.print("    ");
                        console.print(pn);
                        console.print(" ");
                        console.print(MString.rep('.', 57 - pn.length()));
                        console.print(" ");
    
                        switch (p.getStatus()) {
                            case FAILURE:
                                console.setColor(COLOR.RED, null);
                                break;
                            case SKIPPED:
                                console.setColor(COLOR.YELLOW, null);
                                break;
                            case SUCCESS:
                                console.setColor(COLOR.GREEN, null);
                                break;
                            default:
                                break;
                        }
                        console.println(p.getStatus());
                        console.cleanup();
                        
                        for (Result sub : subSteps) {
                            if (sub.status == STATUS.SKIPPED)
                                console.setColor(COLOR.BRIGHT_BLACK, null);
                            else 
                                console.setColor(COLOR.WHITE, null);
                            console.print("    ");
                            console.print(MString.rep(' ', sub.indent*2));
                            console.print(sub.step.getTitle());
                            console.print(" ");
                            console.print(MString.rep('.', 55 - (sub.indent*2 + sub.step.getTitle().length() ) ) );
                            console.print(" ");
                            switch (sub.status) {
                            case FAILURE:
                                console.setColor(COLOR.RED, null);
                                break;
                            case SKIPPED:
                                console.setColor(COLOR.YELLOW, null);
                                break;
                            case SUCCESS:
                                console.setColor(COLOR.GREEN, null);
                                break;
                            default:
                                break;
	                        }
	                        console.println(sub.status);
	                        console.cleanup();
                        }
                    }
                }
            }
        }

        /* Per Project view
                for (Project p : con.getProjects()) {
                    String name = p.getName() + " ";
                    console.print("  ");
                    console.print(name);
                    console.print(MString.rep('.', 60 - name.length()));
                    console.print(" ");
                    switch (p.getStatus()) {
                    case FAILURE:
                        console.setColor(COLOR.RED, null);
                        isError = true;
                        break;
                    case SKIPPED:
                        console.setColor(COLOR.YELLOW, null);
                        break;
                    case SUCCESS:
                        console.setColor(COLOR.GREEN, null);
                        break;
                    default:
                        break;
                    }
                    console.println(p.getStatus());
                    console.cleanup();
                    for (Result result : results) {
                        if (result.project != null && result.project.getName().equals(p.getName())) {
                            String stepTitle = result.step.getTitle();
                            console.print("    ");
                            console.setColor(COLOR.BRIGHT_BLACK, null);
                            console.print(stepTitle);
                            console.print(" ");
                            console.print(MString.rep('.', 57 - stepTitle.length()));
                            console.print(" ");
                            switch (result.status) {
                            case FAILURE:
                                console.setColor(COLOR.RED, null);
                                isError = true;
                                break;
                            case SUCCESS:
                                console.setColor(COLOR.GREEN, null);
                                break;
                            default:
                                break;
                            }
                            console.println(result.status);
                            console.cleanup();
                        }
                    }
                }
        */
        console.println();
        console.setBold(true);
        console.println("------------------------------------------------------------------------");
        console.setBold(false);
        if (!errors.isEmpty()) {
            for (ErrorInfo error : errors) {
                console.setColor(COLOR.RED, null);
                System.out.println("ERROR");
                System.out.println("    Step   : " + error.getContext().getStep());
                if (error.getContext().getProject() != null)
                    System.out.println("    Project: " + error.getContext().getProject());
                System.out.println("    Plugin : " + error.getContext().getPlugin().getTarget());
                if (error.getError() != null) {
                    console.setColor(COLOR.YELLOW, null);
                    System.out.println("Error:");
                    error.getError().printStackTrace();
                }
            }
        } else {
            if (isError) {
                console.setColor(COLOR.RED, null);
                console.println("  BUILD FAILED");
            } else {
                console.setColor(COLOR.GREEN, null);
                console.println("  BUILD SUCCESS");
            }
        }
        console.cleanup();
        console.setBold(true);
        console.println("------------------------------------------------------------------------");
        console.setBold(false);
        console.print("    Total Time: ");
        console.println(watch.getCurrentTimeAsString());
        console.print("    Finished at: ");
        console.println(MDate.toIsoDateTime(watch.getStop()));
        console.setBold(true);
        console.println("------------------------------------------------------------------------");
        console.setBold(false);
    }

    private STATUS getStepStatus(Step step) {
        STATUS status = STATUS.SKIPPED;
        for (Result result : results)
            if (result.step.getId() == step.getId()) {
                if (result.status == STATUS.SUCCESS) status = result.status;
                if (result.status == STATUS.FAILURE) return STATUS.FAILURE;
            }
        return status;
    }

    @Override
    public void executeBegin(Conductor con, String lifecycle, Steps steps) {
        results = new LinkedList<>();
        watch = new MStopWatch().start();
    }

    static class Result {

        private Step step;
        private Step parent;
        private Step main;
        private Project project;
        private STATUS status;
        private int indent;

        public Result(Project.STATUS status, Context context, LinkedList<Step> subSteps) {
            this.step = context.getStep();
            this.project = context.getProject();
            this.status = status;
            if (subSteps.size() > 0) {
                this.parent = subSteps.getLast();
                this.main = subSteps.getFirst();
            } else {
                this.main = step;
            }
            this.indent = subSteps.size();
        }
    }

    @Override
    public void enterSubSteps(Conductor con, Step step) {
        subSteps.add(step);
    }

    public Step getParentStep() {
        if (subSteps.size() == 0) return null;
        return subSteps.getLast();
    }

    @Override
    public void leaveSubSteps(Conductor con, Step step) {
        Step s = subSteps.removeLast();
        if (s != step)
            log().d("enter and leave are not the same steps",s,step);
    }
}
