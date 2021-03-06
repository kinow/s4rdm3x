---
title: 's4rdm3x: A Tool Suite to Explore Code to Architecture Mapping Techniques'
tags:
  - Java
  - orphan adoption
  - clustering
  - reflexion modeling
  - architecture conformance checking
authors:
  - name: Tobias Olsson
    orcid: 0000-0003-1154-5308
    affiliation: 1
  - name: Morgan Ericsson
    orcid: 0000-0003-1173-5187
    affiliation: 1
  - name: Anna Wingkvist
    orcid: 0000-0002-0835-823X
    affiliation: 1
affiliations:
 - name: Department of Computer Science and Media Technology, Linnaeus University, Sweden
   index: 1
date: 13 April 2020
bibliography: paper.bib

---

# Summary

Architectural drift and erosion, where the implementation starts to deviate from the intended software architecture or the rules set by it, are common problems in long-lived software systems. This can be avoided by using techniques, such as Reflexion modeling [@murphy1995software], to validate that the implementation conforms to the indented architecture. Unfortunately, such techniques require a mapping from source code modules (e.g., classes) to elements of the architecture, something that is not always available or up to date. This is a known problem when, e.g., companies want to adopt static architecture conformance checking; the effort to manually create or bring this mapping up to date is just too time-consuming and error-prone [@Ali2017ArchitectureRequirements; @InfoRetrieval].

The ``s4rdm3x`` tool suite is designed for researchers and practitioners to study and evaluate algorithms that perform part of the mapping automatically, such as orphan-adoption clustering [@HuGMe] or information retrieval techniques [@InfoRetrieval]. It includes a graphical user interface to define and run experiments with mapping algorithms and their parameters, and visualize and explore the results of these experiments. The experiments can be executed locally or in a remote high-performance computing environment. The tool suite includes reference implementations of state of the art mapping algorithms and a set of Java systems with validated mappings between classes and architecture elements. The tool suite is extensible, so it is easy to add new mapping algorithms and visualizations to explore their performance.

# Statement of Need
To faciliate the further development and evaluiation of mapping techniques the software provides reference implementations of the current state-of-the-art mapping techniques and the means to implement new techniques and run experiments. It includes the HuGMe orphan adoption clustering method [@HuGMe], and four attraction functions to decide which architectural element a source code module should be mapped to: `CountAttract` [@HuGMe], `IRAttract`, `LSIAttract` [@InfoRetrieval] and `NBAttract` [@NaiveBayes]. There is also a reference implementation of our novel technique to create a textual representation of source code dependencies at an architectural level; Concrete Dependency Abstraction (CDA). It also contains a set of validated mappings between source code classes and architectural elements that are often used in software architecture erosion research. These systems have either been recovered from replication packages [@brunet2012evolutionary; @LenhardExploringSCMIndicatingArchInconsistency] or the [SAEroCon workshop repository](https://github.com/sebastianherold/SAEroConRepo). 

# The ``s4rdm3x`` Tool Suite

``S4rdm3x`` is an extensible suite of tools for source code analysis, architecture definition, mapping of source code modules to architecture elements, experiment definitions, and exploratory and visual analysis. The suite consists of an *extensible core* and two tools, a *graphical editor* to create and visualize mapping experiments and a *command-line tool to run experiment* at scale. 

![Overview of the s4rdm3x Core showing the implementation metamodel, and the mapper and experiment subsystem](classes.pdf)

The *core* provides Java bytecode analysis to extract a dependency graph (and naming information) as well as loading an architectural definition and source to module mapping. Figure 1 provides an overview of the important classes in the core and their main dependencies; the three leftmost classes represent source code modules, and their implemented dependencies contained as a graph and the architecture is represented by components and their allowed dependencies. There is a rich set of dependencies that are extracted from (Java) byte code, including the possibility to include implicit dependencies found via hard-coded constants. The `MapperBase` class is used to implement different mapping strategies as subclasses. `MapperExperiment` provides functionality to set up and run mapping experiments using combinations of random parameters at different intervals. An experiment is implemented as a subclass of `MapperExperiment` that instantiates the corresponding subclasses of `MapperBase`. This means that the mappers are not exclusive for experiments and can be reused in other situations, e.g., in a tool that performs semi-automatic mapping as part of a reflexion modeling approach. 

The *graphical editor* is used to define, visualize, analyze, and compare how well mapping algorithms perform with different parameters and initial sets of known mappings. It supports a range of visualizations and can be extended with new ones. The editor uses an Immediate Mode GUI approach, where the application renders the graphical primitives it needs (e.g., lines, rectangles, and points) every frame, an approach often used in computer games and tools used for computer game development since it offers fine-grained control over the visualization. This fine-grained control makes it possible to extend the editor with custom visualizations. The GUI uses [OpenGL](https://opengl.org) to provide hardware-accelerated rendering. 

The graphical editor can be used to run experiments, but these generally require a large number of combinations of, e.g., parameters, initial sets, and systems, so they can take a long time to run. The suite includes a command-line tool that runs these combinations in parallel on many-core machines. The command-line tool can read experiment definitions in XML exported from the graphical editor and save the results in a format that can be imported and visualized.
 


``S4rdm3x`` is implemented in Java and depends on [ASM](https://asm.ow2.io), [Weka](https://www.cs.waikato.ac.nz/ml/weka), and [Dear JVM ImGui](https://github.com/kotlin-graphics/imgui).

# Applications

The ``S4rdm3x`` tool suite has been used in research studies on orphan adoption [@ImprovedHuGMe; @NaiveBayes] and as a continuous integration tool-chain for static architecture conformance checking of student project submissions. 

# Acknowledgments

This work is supported by the [Linnaeus University Centre for Data Intensive Sciences and Applications (DISA)](https://lnu.se/forskning/sok-forskning/linnaeus-university-centre-for-data-intensive-sciences-and-applications) High-Performance Computing Center.  

# References
