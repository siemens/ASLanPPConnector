// Copyright 2010-2013 (c) IeAT, Siemens AG, AVANTSSAR and SPaCIoS consortia.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.avantssar.aslanpp.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.avantssar.aslanpp.ASLanPPNewLexer;
import org.avantssar.aslanpp.ASLanPPNewParser;
import org.avantssar.aslanpp.Debug;
import org.avantssar.aslanpp.SymbolsNew;
import org.avantssar.aslanpp.ToASLanNew;
import org.avantssar.aslanpp.visitors.DummySymbolsCreator;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.aslanpp.visitors.PostProcessor;
import org.avantssar.aslanpp.visitors.Preprocessor;
import org.avantssar.aslanpp.visitors.PrettyPrinter;
import org.avantssar.aslanpp.visitors.TypeAssigner;
import org.avantssar.aslanpp.visitors.Validator;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class ASLanPPSpecification extends GenericScope implements IEntityOwner, ICommentsHolder {

    private static String sourceName;
    private String specName;
    private ChannelModel cm;
    private Entity rootEnt;
    private final ErrorGatherer err = new ErrorGatherer(ErrorMessages.DEFAULT);
    private final CommentsHolder comments;

    private final EntityManager manager;

    public static ASLanPPSpecification fromStream(EntityManager manager, String fileName, InputStream aslanppSpec, ErrorGatherer err) throws IOException, RecognitionException {
        if (fileName != null) {
            sourceName = new java.io.File(fileName).getName();
            if (sourceName != null) {
                // remove any file name extension
                int lastindex = sourceName.lastIndexOf('.');
                if (lastindex >= 0)
                    sourceName = sourceName.substring(0, lastindex);
            }
        }
        // Run the lexer first.
        ANTLRInputStream antStream = new ANTLRInputStream(aslanppSpec);
        if (err == null)
          err = new ErrorGatherer(ErrorMessages.DEFAULT);
        ASLanPPNewLexer lexer = new ASLanPPNewLexer(antStream);
        lexer.setErrorGatherer(err);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ASLanPPNewParser parser = new ASLanPPNewParser(tokens);
        parser.setErrorGatherer(err);
        ASLanPPNewParser.program_return r = null;
        r = parser.program(manager);
        Debug.logger.info("Parser reported " + parser.getNumberOfSyntaxErrors() + " errors.");
        ASLanPPSpecification spec = r.spec;
        if (spec != null) try {
            spec.getErrorGatherer().addAll(err); // copy any errors from parsing phase into the new instance.
            if(parser.getNumberOfSyntaxErrors() == 0 && r.getTree() != null) {
                // By this time the types are registered, so we can run the
                // tree grammar that will register the symbols.
                CommonTree ct = (CommonTree) r.getTree();
                CommonTreeNodeStream nodes = new CommonTreeNodeStream(ct);
                SymbolsNew symb = new SymbolsNew(nodes);
                symb.entity(spec);
                // Now we can run the tree grammar that will load the
                // expressions and types into the in-memory model.
                nodes.reset();
                ToASLanNew ta = new ToASLanNew(nodes);
                ta.entity(spec);
            }
        }
        finally {
                err.addAll(spec.getErrorGatherer()); //copy back any errors 
        }
        return spec;
    }

    private ChannelModel str2cm(LocationInfo cmloc, String cmstr) {
        try {
            return ChannelModel.valueOf(cmstr);
        } catch (Exception ex) {
            ChannelModel cm = ChannelModel.CCM;
            StringBuffer msg = new StringBuffer();
            boolean first = true;
            for (ChannelModel opt : ChannelModel.values()) {
                if (!first) {
                    msg.append(", ");
                }
                msg.append(opt.toString());
                first = false;
            }
            getErrorGatherer().addError(cmloc, ErrorMessages.INVALID_CHANNEL_MODEL_CODE, cmstr, msg.toString(), cm.toString());
            return cm;
        }
    }

    public ASLanPPSpecification(EntityManager manager, LocationInfo nameloc, String name, LocationInfo cmloc, String cmstr) {
        this(manager, name, ChannelModel.CCM); // I was forced to use preliminary cm entry
        cm = str2cm(cmloc, cmstr);
        if (cm != ChannelModel.CCM) purge(); // hack to re-initialize the Prelude entries
        if (sourceName != null && !sourceName.equals(name)) {
            err.addWarning(nameloc, ErrorMessages.INVALID_SPECIFICATION_NAME, name, sourceName);
        }
    }

    public ASLanPPSpecification(EntityManager manager, String name, ChannelModel cm) {
        super(null, "");
        specName = name;
        this.cm = cm;
        this.comments = new CommentsHolder(err);
        this.manager = manager;
        purge();
    }

    @Override
    public ErrorGatherer getErrorGatherer() {
        return err;
    }

    public void finalize(boolean skipPublicAndInvertibleHornClauses) {
        // expand and then remove macros
        preprocess();
        // create dummy symbols
        createDummySymbols();
        // check types and assign types to symbols
        assignTypes();
        // add any auxiliary symbols, like dummy symbols for matches, horn
        // clauses for public/invertible functions, etc.
        postprocess(skipPublicAndInvertibleHornClauses);
        // validate and fix what can be fixed (e.g. matches that are not matched
        // in all places in a term)
        validate();
    }

    private void preprocess() {
        accept(new Preprocessor(err));
    }

    private void createDummySymbols() {
        accept(new DummySymbolsCreator(err));
    }

    private void postprocess(boolean skipPublicAndInvertibleHornClauses) {
        PostProcessor pp = new PostProcessor(err);
        pp.setSkipPublicAndInvertibleHornClauses(skipPublicAndInvertibleHornClauses);
        accept(pp);
    }

    private void assignTypes() {
        accept(new TypeAssigner(err));
    }

    private void validate() {
        accept(new Validator(err));
    }

    public PrettyPrinter toStream(OutputStream out) throws IOException {
        return toStream(out, false);
    }

    public PrettyPrinter toStream(OutputStream out, boolean showInternals) throws IOException {
        PrettyPrinter pp = new PrettyPrinter(showInternals);
        accept(pp);
        out.write(pp.toString().getBytes());
        return pp;
    }

    public PrettyPrinter toFile(String path) throws IOException {
        return toFile(path, false);
    }

    public PrettyPrinter toFile(String path, boolean showInternals) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        return toStream(fos, showInternals);
    }

    @Override
    public String toString() {
        PrettyPrinter pp = new PrettyPrinter();
        accept(pp);
        return pp.toString();
    }

    public void accept(IASLanPPVisitor visitor) {
        visitor.visit(this);
    }

    public void setRootEntity(Entity ent) {
        rootEnt = ent;
    }

    public String getSpecificationName() {
        return specName;
    }

    public void setSpecificationName(String specificationName) {
        this.specName = specificationName;
    }

    public ChannelModel getChannelModel() {
        return cm;
    }

    public Entity getRootEntity() {
        return rootEnt;
    }

    @Override
    public void purge() {
        super.purge();
        Prelude.registerDefaultTypesAndSymbols(this, cm);
    }

    public Entity entity(String name) {
        return new Entity(manager, this, name, cm);
    }

    public void addCommentLine(String comment, LocationInfo location) {
        comments.addCommentLine(comment, location);
    }

    public List<MetaInfo> getMetaInfo() {
        return comments.getMetaInfo();
    }
}
