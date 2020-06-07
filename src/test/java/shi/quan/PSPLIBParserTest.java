package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.psplib.PSPLIBBaseListener;
import shi.quan.psplib.PSPLIBLexer;
import shi.quan.psplib.PSPLIBParser;
import shi.quan.vo.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

@QuarkusTest
public class PSPLIBParserTest {
    private static final Logger logger = LoggerFactory.getLogger(PSPLIBParserTest.class);

    private static boolean verbose = true;

    @Test
    public void test() throws IOException {
        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/j120.sm/j1201_1.sm");

        PSPModel data = new PSPModel();

        Stack<PSPNode> stack = new Stack<>();

        CharStream stream = CharStreams.fromStream(ins);

        PSPLIBLexer lexer = new PSPLIBLexer(stream);

        PSPLIBParser parser = new PSPLIBParser(new CommonTokenStream(lexer));

        parser.addParseListener(new PSPLIBBaseListener() {
            @Override
            public void enterFile_header(PSPLIBParser.File_headerContext ctx) {
//                if (verbose) logger.info("[enterFile_header] text : {}", ctx.getText());
                stack.push(data);
            }

            @Override
            public void exitFile_header(PSPLIBParser.File_headerContext ctx) {
//                if (verbose) logger.info("[exitFile_header] text : {}", ctx.getText());
                stack.pop();
            }

            @Override
            public void enterHeader_info(PSPLIBParser.Header_infoContext ctx) {
//                if (verbose) logger.info("[enterHeader_info] text : {}", ctx.getText());
                stack.push(data);
            }

            @Override
            public void exitHeader_info(PSPLIBParser.Header_infoContext ctx) {
//                if (verbose) logger.info("[exitHeader_info] text : {}", ctx.getText());
                stack.pop();
            }

            @Override
            public void exitRenewable_number(PSPLIBParser.Renewable_numberContext ctx) {
//                if (verbose) logger.info("[exitRenewable_number] text : {}", ctx.getText());
                data.setRenewableResourceCount(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitNonrenewable_number(PSPLIBParser.Nonrenewable_numberContext ctx) {
//                if (verbose) logger.info("[exitNonrenewable_number] text : {}", ctx.getText());
                data.setNonRenewableResourceCount(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitDoublyconstrained_number(PSPLIBParser.Doublyconstrained_numberContext ctx) {
//                if (verbose) logger.info("[exitDoublyconstrained_number] text : {}", ctx.getText());
                data.setDoublyConstrainedResourceCount(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void enterGeneral_information(PSPLIBParser.General_informationContext ctx) {
//                if (verbose) logger.info("[enterGeneral_information] text : {}", ctx.getText());
                stack.push(new PSPGeneralInformation());
            }

            @Override
            public void exitGeneral_information(PSPLIBParser.General_informationContext ctx) {
//                if (verbose) logger.info("[exitGeneral_information] text : {}", ctx.getText());
                data.getGeneralInformationList().add((PSPGeneralInformation) stack.pop());
            }

            @Override
            public void enterRelationships(PSPLIBParser.RelationshipsContext ctx) {
//                if (verbose) logger.info("[enterRelationships] text : {}", ctx.getText());
                stack.push(new PSPRelationship());
            }

            @Override
            public void exitRelationships(PSPLIBParser.RelationshipsContext ctx) {
//                if (verbose) logger.info("[exitRelationships] text : {}", ctx.getText());
                data.getRelationshipList().add((PSPRelationship) stack.pop());
            }

            @Override
            public void enterRequests_durations(PSPLIBParser.Requests_durationsContext ctx) {
//                if (verbose) logger.info("[enterRequests_durations] text : {}", ctx.getText());
                stack.push(data.getResources());
            }

            @Override
            public void exitRequests_durations(PSPLIBParser.Requests_durationsContext ctx) {
//                if (verbose) logger.info("[exitRequests_durations] text : {}", ctx.getText());
                stack.pop();
            }

            @Override
            public void enterResources(PSPLIBParser.ResourcesContext ctx) {
//                if (verbose) logger.info("[enterResources] text : {}", ctx.getText());
                stack.push(new PSPResource());
            }

            @Override
            public void exitResources(PSPLIBParser.ResourcesContext ctx) {
//                if (verbose) logger.info("[exitResources] text : {}", ctx.getText());
                data.getResources().getResourceList().add((PSPResource) stack.pop());
            }

            @Override
            public void enterResource_availabilities(PSPLIBParser.Resource_availabilitiesContext ctx) {
//                if (verbose) logger.info("[enterResource_availabilities] text : {}", ctx.getText());
                stack.push(data.getAvailabilities());
            }

            @Override
            public void exitResource_availabilities(PSPLIBParser.Resource_availabilitiesContext ctx) {
//                if (verbose) logger.info("[exitResource_availabilities] text : {}", ctx.getText());
                stack.pop();
            }

            @Override
            public void enterAvailablities(PSPLIBParser.AvailablitiesContext ctx) {
//                if (verbose) logger.info("[enterAvailablities] text : {}", ctx.getText());
                stack.push(data.getAvailabilities());
            }

            @Override
            public void exitAvailablities(PSPLIBParser.AvailablitiesContext ctx) {
//                if (verbose) logger.info("[exitAvailablities] text : {}", ctx.getText());
                stack.pop();
            }

            @Override
            public void exitAvailability(PSPLIBParser.AvailabilityContext ctx) {
//                if (verbose) logger.info("[exitAvailability] text : {}", ctx.getText());
                PSPAvailabilities availabilities = (PSPAvailabilities)stack.peek();
                availabilities.getAvailabilityList().add(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitProNumber(PSPLIBParser.ProNumberContext ctx) {
//                if (verbose) logger.info("[exitProNumber] text : {}", ctx.getText());
                ((PSPGeneralInformation)stack.peek()).setProjectNumber(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitJobId(PSPLIBParser.JobIdContext ctx) {
//                if (verbose) logger.info("[exitJobId] text : {}", ctx.getText());

                int jobId = Integer.parseInt(ctx.getText());

                PSPNode node = stack.peek();

                if(node instanceof PSPGeneralInformation) {
                    PSPGeneralInformation generalInformation = (PSPGeneralInformation)node;
                    generalInformation.setJobCount(jobId);
                } else if(node instanceof PSPRelationship) {
                    PSPRelationship relationship = (PSPRelationship)node;
                    relationship.setJobId(jobId);
                } else if(node instanceof PSPResource) {
                    PSPResource resource = (PSPResource)node;
                    resource.setJobId(jobId);
                } else {
                    throw new RuntimeException("Bad type...");
                }
            }

            @Override
            public void exitRealDate(PSPLIBParser.RealDateContext ctx) {
//                if (verbose) logger.info("[exitRealDate] text : {}", ctx.getText());
                ((PSPGeneralInformation)stack.peek()).setRelDate(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitDueDate(PSPLIBParser.DueDateContext ctx) {
//                if (verbose) logger.info("[exitDueDate] text : {}", ctx.getText());
                ((PSPGeneralInformation)stack.peek()).setDueDate(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitTardCost(PSPLIBParser.TardCostContext ctx) {
//                if (verbose) logger.info("[exitTardCost] text : {}", ctx.getText());
                ((PSPGeneralInformation)stack.peek()).setTardCost(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitMpmTime(PSPLIBParser.MpmTimeContext ctx) {
//                if (verbose) logger.info("[exitMpmTime] text : {}", ctx.getText());
                ((PSPGeneralInformation)stack.peek()).setMpmTime(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitModes(PSPLIBParser.ModesContext ctx) {
//                if (verbose) logger.info("[exitModes] text : {}", ctx.getText());

                int mode = Integer.parseInt(ctx.getText());

                PSPNode node = stack.peek();

                if(node instanceof PSPRelationship) {
                    PSPRelationship relationship = (PSPRelationship)node;
                    relationship.setModes(mode);
                } else if(node instanceof PSPResource) {
                    PSPResource resource = (PSPResource)node;
                    resource.setMode(mode);
                } else {
                    throw new RuntimeException("Bad type...");
                }
            }

            @Override
            public void exitSuccessorNumber(PSPLIBParser.SuccessorNumberContext ctx) {
//                if (verbose) logger.info("[exitSuccessorNumber] text : {}", ctx.getText());
                ((PSPRelationship)stack.peek()).setSuccessorCount(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitSuccessor(PSPLIBParser.SuccessorContext ctx) {
//                if (verbose) logger.info("[exitSuccessor] text : {}", ctx.getText());
                ((PSPRelationship)stack.peek()).getSuccessorList().add(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitResourceNumber(PSPLIBParser.ResourceNumberContext ctx) {
//                if (verbose) logger.info("[exitResourceNumber] text : {}", ctx.getText());

                int resourceNumber = Integer.parseInt(ctx.getText());

                PSPNode node = stack.peek();

                if(node instanceof PSPAvailabilities) {
                    PSPAvailabilities availabilities = (PSPAvailabilities)node;
                    availabilities.getNameList().add(resourceNumber);
                } else if(node instanceof PSPResources) {
                    PSPResources resources = (PSPResources)node;
                    resources.getNameList().add(resourceNumber);
                } else {
                    throw new RuntimeException("Bad type...");
                }
            }

            @Override
            public void exitDuration(PSPLIBParser.DurationContext ctx) {
//                if (verbose) logger.info("[exitDuration] text : {}", ctx.getText());
                ((PSPResource)stack.peek()).setDuration(Integer.parseInt(ctx.getText()));
            }

            @Override
            public void exitResource(PSPLIBParser.ResourceContext ctx) {
//                if (verbose) logger.info("[exitResource] text : {}", ctx.getText());
                ((PSPResource)stack.peek()).getResourceList().add(Integer.parseInt(ctx.getText()));
            }

            private String key = null;
            private String value = null;

            @Override
            public void enterProperty(PSPLIBParser.PropertyContext ctx) {
//                if (verbose) logger.info("[enterProperty] text : {}", ctx.getText());
                this.key = null;
                this.value = null;
            }

            @Override
            public void exitProperty(PSPLIBParser.PropertyContext ctx) {
//                if (verbose) logger.info("[exitProperty] text : {}", ctx.getText());
                ((PSPModel)stack.peek()).getPropMap().put(key, value);

                this.key = null;
                this.value = null;
            }

            @Override
            public void exitKey(PSPLIBParser.KeyContext ctx) {
//                if (verbose) logger.info("[exitKey] text : {}", ctx.getText());
                this.key = ctx.getText();
            }

            @Override
            public void exitValue(PSPLIBParser.ValueContext ctx) {
//                if (verbose) logger.info("[exitValue] text : {}", ctx.getText());
                this.value = ctx.getText();
            }

            @Override
            public void visitErrorNode(ErrorNode node) {
                if (verbose) logger.error("[visitErrorNode] text : {}", node.getText());
            }
        });

        parser.model();

        ins.close();

        logger.info("data : {}", data);
    }
}