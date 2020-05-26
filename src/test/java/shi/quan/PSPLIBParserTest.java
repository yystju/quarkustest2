package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.psplib.PSPLIBBaseListener;
import shi.quan.psplib.PSPLIBLexer;
import shi.quan.psplib.PSPLIBParser;

import java.io.IOException;
import java.io.InputStream;

@QuarkusTest
public class PSPLIBParserTest {
    private static final Logger logger = LoggerFactory.getLogger(PSPLIBParserTest.class);

    @Test
    public void test() throws IOException {
        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/j120.sm/j1201_1.sm");

        CharStream stream = CharStreams.fromStream(ins);

        PSPLIBLexer lexer = new PSPLIBLexer(stream);

        PSPLIBParser parser = new PSPLIBParser(new CommonTokenStream(lexer));

        parser.addParseListener(new PSPLIBBaseListener() {
            @Override
            public void enterModel(PSPLIBParser.ModelContext ctx) {
                logger.info("[enterModel] {}", ctx.getText());
                super.enterModel(ctx);
            }

            @Override
            public void enterResource(PSPLIBParser.ResourceContext ctx) {
                logger.info("[enterResource] {}", ctx.getText());
                super.enterResource(ctx);
            }

            @Override
            public void enterProjectInformation(PSPLIBParser.ProjectInformationContext ctx) {
                logger.info("[enterProjectInformation] {}", ctx.getText());
                super.enterProjectInformation(ctx);
            }

            @Override
            public void enterPrecedenceRelations(PSPLIBParser.PrecedenceRelationsContext ctx) {
                logger.info("[enterPrecedenceRelations] {}", ctx.getText());
                super.enterPrecedenceRelations(ctx);
            }

            @Override
            public void enterRequestsDurations(PSPLIBParser.RequestsDurationsContext ctx) {
                logger.info("[enterRequestsDurations] {}", ctx.getText());
                super.enterRequestsDurations(ctx);
            }

            @Override
            public void enterResourceAvailabilities(PSPLIBParser.ResourceAvailabilitiesContext ctx) {
                logger.info("[enterResourceAvailabilities] {}", ctx.getText());
                super.enterResourceAvailabilities(ctx);
            }

            @Override
            public void exitHeaders(PSPLIBParser.HeadersContext ctx) {
                logger.info("[exitHeaders] {}", ctx.getText());
                super.enterHeaders(ctx);
            }

            @Override
            public void exitData(PSPLIBParser.DataContext ctx) {
                logger.info("[exitData] {}", ctx.getText());
                super.enterData(ctx);
            }

            @Override
            public void exitProp(PSPLIBParser.PropContext ctx) {
                logger.info("[exitProp] {}", ctx.getText());
                super.enterProp(ctx);
            }
        });

        PSPLIBParser.ModelContext model = parser.model();

        ParseTreeWalker walker = new ParseTreeWalker();

        walker.walk(new ParseTreeListener() {
            @Override
            public void visitTerminal(TerminalNode terminalNode) {
                logger.info("[visitTerminal] terminalNode : {}", terminalNode.getText());
            }

            @Override
            public void visitErrorNode(ErrorNode errorNode) {
                logger.info("[visitErrorNode] errorNode : {}", errorNode.getText());
            }

            @Override
            public void enterEveryRule(ParserRuleContext parserRuleContext) {
                logger.info("[enterEveryRule] text : {}", parserRuleContext.getText());
            }

            @Override
            public void exitEveryRule(ParserRuleContext parserRuleContext) {

            }
        }, model);

        ins.close();
    }
}