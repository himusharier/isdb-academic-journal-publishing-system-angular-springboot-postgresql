import 'package:flutter/material.dart';

/// Contains the original homepage content, including the banner and text.
class HomePageContent extends StatelessWidget {
  const HomePageContent({super.key});

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          // Image.asset('assets/images/homepage-banner.jpg'),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(12.0),
              child: Image.asset(
                'assets/images/homepage-banner.jpg',
                height: 200,
                fit: BoxFit.fitHeight,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: 16.0,
              vertical: 24.0,
            ),
            child: Column(
              children: <Widget>[
                Padding(
                  padding: const EdgeInsets.only(bottom: 24.0),
                  child: Text(
                    'Welcome',
                    style: Theme.of(context).textTheme.headlineLarge,
                    textAlign: TextAlign.center,
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.only(bottom: 16.0),
                  child: Text.rich(
                    TextSpan(
                      text:
                          'ScholarPress is committed to prioritizing quality over quantity in its publications. Our open-access platform ensures researchers worldwide can freely access newly published findings. All submissions undergo plagiarism screening using tools like ',
                      children: <TextSpan>[
                        TextSpan(
                          text: 'CrossCheck',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                        const TextSpan(text: ' and '),
                        TextSpan(
                          text: 'Dupli Checker',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                        const TextSpan(
                          text:
                              '. If significant similarities are found, particularly in the results or discussion sections, the manuscript will be returned to the author(s). Please refer to our plagiarism policy available in our journals for detailed guidelines.',
                        ),
                      ],
                    ),
                    style: Theme.of(context).textTheme.bodyLarge,
                    textAlign: TextAlign.justify,
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.only(bottom: 16.0),
                  child: Text.rich(
                    TextSpan(
                      text:
                          'In addition to journals, ScholarPress also publishes books in the fields of ',
                      children: <TextSpan>[
                        TextSpan(
                          text: 'BioSciences',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                        const TextSpan(text: ' and '),
                        TextSpan(
                          text: 'Environmental Sciences',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                        const TextSpan(
                          text:
                              ', under open-access or hybrid models. These books are distributed globally through both online and offline channels. We do not publish theses or project reports in our journals. All materials are evaluated in line with our plagiarism policy. We encourage authors to ensure their work contains minimal or no similarity before submission.',
                        ),
                      ],
                    ),
                    style: Theme.of(context).textTheme.bodyLarge,
                    textAlign: TextAlign.justify,
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.only(bottom: 16.0),
                  child: Text(
                    'To support authors, we offer paid editorial services including improvement of writing quality, grammar correction, and enhancement of sentence clarity and engagement. Our editorial team also assists in formatting manuscripts to meet international referencing or journal style requirements.',
                    style: Theme.of(context).textTheme.bodyLarge,
                    textAlign: TextAlign.justify,
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.only(bottom: 32.0),
                  child: Text(
                    'Our online submission system is user-friendly, streamlined, and does not require separate systems for each journal. Authors should mention the target journal in their cover letter, and our experienced team will manage the process accordingly—providing updates from acknowledgment to final decision.',
                    style: Theme.of(context).textTheme.bodyLarge,
                    textAlign: TextAlign.justify,
                  ),
                ),
                Text(
                  'Stay with ScholarPress — Stay with Quality Publishing',
                  style: Theme.of(context).textTheme.headlineMedium,
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
